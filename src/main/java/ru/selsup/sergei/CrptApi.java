package ru.selsup.sergei;


import lombok.Data;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The `CrptApi` class provides functionality to interact with the Honest Sign API.
 * It is a thread-safe class that supports limiting the number of requests to the API.
 * The request limit is specified in the constructor as the maximum number of requests within a certain time interval.
 */
public class CrptApi {

    /**
     * The base URL of the Honest Sign API.
     */
    private final String URL = "https://ismp.crpt.ru";

    /**
     * The client token for accessing the Honest Sign API.
     */
    private final String CLIENT_TOKEN = "clientToken";

    /**
     * The username for authenticating with the Honest Sign API.
     */
    private final String USER_NAME = "userName";

    /**
     * The service for accessing the Honest Sign API.
     */
    private final AccessingApiService<HonestSignApiService> accessingHonestSignApiService;

    /**
     * Constructs a new `CrptApi` object with the specified request limit.
     *
     * @param timeUnit      the time unit for the speed measurement time interval
     * @param requestLimit  the maximum number of requests allowed within the time interval
     * @throws IllegalArgumentException if the request limit is less than or equal to zero
     */
    public CrptApi(TimeUnit timeUnit, int requestLimit) {

        this.accessingHonestSignApiService = new AccessingApiService<>(
                timeUnit, requestLimit, new HonestSignApiService(URL, CLIENT_TOKEN, USER_NAME)
        );
    }

    /**
     * Creates a document for a Russian product and submits it to the Honest Sign API.
     *
     * @param document   the document object representing the product information
     * @param signature  the signature string for the document
     */
    public void createDocumentForRussianProduct(Document document, String signature) {

        HonestSignApiService honestSignApiService = accessingHonestSignApiService.get();

        honestSignApiService.createDocumentForRussianProduct(document, signature);
    }


    /**
     * The `AccessingApiService` class provides control over accessing the Honest Sign API
     * based on the specified request limit and speed measurement time interval.
     *
     * @param <T>  the type of API service
     */
    public static class AccessingApiService<T> {
        private final T service;
        private final long speedMeasurementTime;
        private final int requestLimit;
        private static final List<Long> requestsTimes = new ArrayList<>();

        public AccessingApiService(TimeUnit timeUnit, int requestLimit, T service) {

            if(requestLimit <= 0 ) {
                throw new IllegalArgumentException("The Request Limit cannot be less than or equal to zero");
            }

            this.service = service;
            this.requestLimit = requestLimit;
            this.speedMeasurementTime = getSpeedMeasurementTime(timeUnit);
        }


        /**
         * Returns the API service instance, respecting the request limit and speed measurement time interval.
         *
         * @return the API service instance
         * @throws RuntimeException if the thread is interrupted while waiting for the request limit
         */
        public synchronized T get() {
            try {
                requestTimeControl(System.currentTimeMillis());

            } catch (InterruptedException exception) {

                throw new RuntimeException(exception);
            }
            return service;
        }

        private void requestTimeControl(long requestTime) throws InterruptedException {

            if(requestsTimes.size() >= requestLimit) {

                long timeDifference = requestTime - requestsTimes.get(0);

                if(timeDifference < speedMeasurementTime) {

                    long sleepTime = speedMeasurementTime - timeDifference;

                    Thread.sleep(sleepTime);
                }
                requestsTimes.remove(0);
            }
            requestsTimes.add(System.currentTimeMillis());
        }

        private long getSpeedMeasurementTime(TimeUnit timeUnit) {

            return switch (timeUnit) {

                case MILLISECONDS -> 1;
                case SECONDS -> 1000;
                case MINUTES -> 1000 * 60;
                case HOURS -> 1000 * 60 * 60;

                default -> throw new IllegalStateException(
                        "Unexpected value: " + timeUnit + ".\n" +
                                "Specify one of the following units of speed measurement: " +
                                "MILLISECONDS, SECONDS, MINUTES, HOURS."
                );
            };
        }
    }


    /**
     * The `HonestSignApiService` class provides methods to interact with the Honest Sign API.
     */
    public static class HonestSignApiService {

        private final String URL;
        private final String CLIENT_TOKEN;
        private final String USER_NAME;
        private final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        public HonestSignApiService(String URL, String CLIENT_TOKEN, String USER_NAME) {
            this.URL = URL;
            this.CLIENT_TOKEN = CLIENT_TOKEN;
            this.USER_NAME = USER_NAME;
        }

        /**
         * Creates a document for a Russian product and sends the request to the Honest Sign API.
         *
         * @param document   the document object representing the product information
         * @param signature  the signature string for the document
         * @return the response from the API
         * @throws RuntimeException if an error occurs during the API request
         */
        public String createDocumentForRussianProduct(Document document, String signature) {

            String documentCreationUrl = URL + "/api/v3/lk/documents/create";

            HttpPost post = new HttpPost(documentCreationUrl);

            List<JSONObject> products = new ArrayList<>();

            for (Product product : document.getProducts()) {

                JSONObject productJson = new JSONObject();

                productJson.put("certificate_document", product.getCertificateDocument());
                productJson.put("certificate_document_date", product.getCertificateDocumentDate());
                productJson.put("certificate_document_number", product.getCertificateDocumentNumber());
                productJson.put("owner_inn", product.getOwnerInn());
                productJson.put("producer_inn", product.getProducerInn());
                productJson.put("production_date", product.getProductionDate());
                productJson.put("tnved_code", product.getTnvedCode());
                productJson.put("uit_code", product.getUitCode());
                productJson.put("uitu_code", product.getUituCode());

                products.add(productJson);
            }

            JSONObject documentJson = new JSONObject();

            documentJson.put("doc_id", document.getDocId());
            documentJson.put("doc_status", document.getDocStatus());
            documentJson.put("doc_type", document.getDocType());
            documentJson.put("importRequest", document.isImportRequest());
            documentJson.put("owner_inn", document.getOwnerInn());
            documentJson.put("participant_inn", document.getParticipantInn());
            documentJson.put("producer_inn", document.getProducerInn());
            documentJson.put("production_date", document.getProductionDate());
            documentJson.put("production_type", document.getProductionType());
            documentJson.put("reg_date", document.getRegDate());
            documentJson.put("reg_number", document.getRegNumber());
            documentJson.put("products", products);


            JSONObject requestBodyJson = new JSONObject();

            requestBodyJson.put("document_format", DocumentFormat.MANUAL);
            requestBodyJson.put("product_document", documentJson);
            requestBodyJson.put("product_group", "group");
            requestBodyJson.put("signature", signature);
            requestBodyJson.put("type", DocType.LP_INTRODUCE_GOODS);


            StringEntity entity;
            CloseableHttpResponse response;

            try {
                entity = new StringEntity(requestBodyJson.toJSONString());
                post.setEntity(entity);

                post.addHeader("content-type", "application/json");
                post.addHeader("clientToken", CLIENT_TOKEN);
                post.addHeader("userName", USER_NAME);

                response = httpClient.execute(post);
                httpClient.close();



                return response.toString();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * The format of the document.
     */
    public enum DocumentFormat { MANUAL, XML, CSV }


    /**
     * The type of code for the product.
     */
    public enum CodeType { UITU, UIT }


    /**
     * The type of document.
     */
    public enum DocType {
        LP_INTRODUCE_GOODS,
        LP_INTRODUCE_GOODS_CSV,
        LP_INTRODUCE_GOODS_XML
    }


    /**
     * The `Document` class represents a document for a Russian product.
     */
    @Data
    public static class Document {

        private String description;
        private int docId;
        private String docStatus;
        private String docType;
        private boolean importRequest;
        private String ownerInn;
        private String participantInn;
        private String producerInn;
        private String productionDate;
        private String productionType;
        private LocalDateTime regDate;
        private String regNumber;
        private List<Product> products = new ArrayList<>();

        public Document(int docId, String docStatus, String docType, String ownerInn,
                        String participantInn, String producerInn, String productionDate,
                        String productionType) {

            this.docId = docId;
            this.docStatus = docStatus;
            this.docType = docType;
            this.ownerInn = ownerInn;
            this.participantInn = participantInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.productionType = productionType;
            this.regDate = LocalDateTime.now();
        }
    }


    /**
     * The `Product` class represents a product within a document.
     */
    @Data
    public static class Product {

        private String certificateDocument;
        private String certificateDocumentDate;
        private String certificateDocumentNumber;
        private  String ownerInn;
        private  String producerInn;
        private String productionDate;
        private String tnvedCode;
        private String uitCode;
        private String uituCode;

        public Product(String ownerInn, String producerInn,
                       String productionDate, String tnvedCode,
                       CodeType type, String code) {

            switch (type) {
                case UITU -> this.uituCode = code;
                case UIT -> this.uitCode = code;
            }

            this.ownerInn = ownerInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.tnvedCode = tnvedCode;
        }
    }
}
