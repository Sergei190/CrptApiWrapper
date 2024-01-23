# CrptApi

The `CrptApi` library provides functionality to interact with the Honest Sign API. It supports rate limiting and thread safety. The library allows users to create documents for Russian products and submit them to the Honest Sign API.

## Table of Contents
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Examples](#examples)
- [Contributing](#contributing)
- [License](#license)

## Features

- Create documents for Russian products and submit them to the Honest Sign API.
- Support for rate limiting to control the number of requests made to the API.
- Thread-safe implementation for concurrent usage.

## Installation

The `CrptApi` library can be installed via [Maven](https://maven.apache.org/) by adding the following dependency to your project's `pom.xml` file:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>crpt-api</artifactId>
    <version>1.0.0</version>
</dependency>
```

Alternatively, you can manually download the JAR file from the releases section.

## Usage

To use the `CrptApi` library in your project, follow these steps:

1. Import the necessary classes:

import com.example.crptapi.CrptApi;
import com.example.crptapi.CrptApi.Document;

2. Create an instance of CrptApi with the desired request limit:

CrptApi api = new CrptApi(TimeUnit.MINUTES, 100);

3. Create a document for a Russian product and submit it to the Honest Sign API:

Document document = new Document();
// Set document properties
// ...

// Submit the document with a signature
String signature = "example_signature";
api.createDocumentForRussianProduct(document, signature);

## Examples

Here is an example of creating a document and submitting it to the Honest Sign API:

// Import necessary classes
import com.example.crptapi.CrptApi;
import com.example.crptapi.CrptApi.Document;

// Create an instance of CrptApi with request limit
CrptApi api = new CrptApi(TimeUnit.MINUTES, 100);

// Create a document
Document document = new Document();
// Set document properties
// ...

// Submit the document with a signature
String signature = "example_signature";
api.createDocumentForRussianProduct(document, signature);

## Contributing

Contributions to the `CrptApi` library are welcome! If you find any issues or have suggestions for 
improvements, feel free to open a pull request. Please ensure that you follow the code of conduct in all interactions.

Before contributing, make sure to:

* Fork the repository and clone it to your local machine.
* Set up your development environment and dependencies.
* Create a new branch with a descriptive name for your feature/bug fix.
* Make your changes, along with appropriate tests and documentation.
* Run the test suite to ensure that all tests pass.
* Submit a pull request describing your changes.