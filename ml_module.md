### Machine Learning Module for DeepFij

## Core Architecture Approach

**Microservices with Event-Driven Communication**

- Spring Boot (Java) handles feature/label generation and business logic
- Flask (Python) manages ML operations and model serving
- Both services will have access to the same database control processes will be through REST API's.
- In general, services should be stateless and idempotent.
- Raw features and labels for training and inference will not be stored in the database but generated on the fly.
- Pipeline definitions and trained parameters will be stored in the model registry.

## Configuration Strategy

**Model Registry & Configuration Management**

**Data Model**
- models 
  - id 
  - name 
  - type
  - description 
  - className 
  - pipeline
  - runnableFeatures
  - runnablePipeline

- model_runs 
  - id 
  - modelId 
  - runDate 
  - runStatus
  - runResult
  
- model_run_params 
  - id 
  - modelRunId 
  - paramName 
  - paramValue

- model_run_metrics
  - id
  - modelRunId 
  - metricName 
  - metricValue

## Data Flow Architecture

1. **Feature Generation** (Spring Boot)
- Processes raw data from primary database
- Applies business logic and feature engineering
- Publishes feature sets via a Flask REST API wrapping an sklearn backend.
1. **ML Pipeline** (Flask)
- Receives request to run pipeline with feature set.
- Trains the model using the feature set.
- Pickles the trained model and saves the result to DB.
- Reponds to rests call with database ID of trained model.
1. **Model Serving** (Flask)
- Receives request to run inference with feature set.
- Loads the model from DB.
- Runs inference on the feature set.
- Returns inference results to client.

## Key Design Patterns to Consider

**Factory Pattern for Models**

- Abstract base model class
- Concrete implementations for different ML libraries
- Configuration-driven instantiation

**Pipeline Pattern for Data Processing**

- Composable data transformation steps
- Easy to add/remove processing stages
- Consistent interface across different model types

**Strategy Pattern for Training/Inference**

- Different strategies for batch vs real-time processing
- Configurable training schedules
- Multiple deployment targets


## Spring Boot Endpoint Design
/models - GET - returns list of models
/models/{id} - GET - returns model details
/models/{id}/train - POST - query variables are passed as used as params to the model training function
/models/{id}/train/{id} - GET - shows model run details
/models/{id}/train/{id} - DELETE - deletes model run
/models/{id}/train/{id}/predict - POST - query variables are passed as used as params to the model inference function
