# Shipment List Demo Application - AWS in PROD and LocalStack on DEV environment

### Prerequisites
- Maven 3.8.5 & Java 17
- AWS free tier account
- Docker - for running LocalStack
- AWS Command Line Interface - for managing your services
- npm - for running the frontend app

## Purpose

This application was conceived for demonstration purposes to highlight the ease of switching from 
using actual AWS dependencies to having them emulated on LocalStack for your *developer environment*.
Of course this comes with other advantages, but the first focus point is making the transition.

## What it does

*shipment-list-demo* is a Spring Boot application dealing with CRUD operations an employee can execute
on a bunch of shipments that they're allowed to view - think of it like the Post app.
The demo consists of a backend and a frontend implementation, using React to display the information.
The AWS services involved are:
 - S3 for storing pictures
 - DynamoDB for the entities
 - Lambda function that will validate the pictures.

## How it works

![Diagram](app_diagram.png)

## How we will be using it
We’ll be walking through a few scenarios using the application, and we expect it to maintain the 
behavior in both production and development environments.
We’ll take advantage of one of the core features of the Spring framework that allows us to bind our 
beans to different profiles, such as dev, test, and prod. Of course, these beans need to know how to 
behave in each environment, so they’ll get that information from their designated configuration files,
`application-prod.yml`, and  `application-dev.yml`.

## Running it

### Production simulation

Now we don’t have a real production environment because that’s not the point here, but most likely, 
an application like this runs on a container orchestration platform, and all the necessary configs 
are still provided. Since we’re only simulating a production instance, all the configurations are 
kept in the `application-prod.yml` file.

Before getting started, it's important to note that an IAM user, who's credentials will be used,
needs to be created with the following policies:
- AmazonS3FullAccess
- AWSLambda_FullAccess
- AmazonDynamoDBFullAccess

The `scripts/new-bucket.sh` script will create the necessary S3 resource.

At startup @dynamobee helps set up the table we need and populate it with some sample data.
@dynamobee is library for tracking, managing, and applying database changes
The changelog acts as a database version control. It tracks all the changes made to the database,
and helps you manage database migration.

To run the backend simply use 
```
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Now `cd` into `src/main/shipment-list-frontend` and run `npm install` and `npm start`.
This will spin up the React app that can be accessed on `localhost:3000`.

You should now be able to see a list of shipments with standard icons, that means that only the database
is populated, the pictures still need to be added from the `sample-pictures` folder.
The weight of a shipment we can perceive, but not the size, that's why we need to display it,
using the "banana for scale" measuring unit. How else would we know??

The Lambda function is still not up. This falls under the `shipment-list-lambda-validator` project.
```
git clone https://github.com/tinyg210/shipment-list-lambda-validator.git
```

The `create-lambda.sh` script will do everything that needs for the creation and configuration of the 
Lambda. (I know what you're thinking, Terraform will follow.)
Run `add-notif-config-for-lambda.sh`, but before that remember to edit `notification-config.json` with
your own AWS account ID. This will enable the Lambda to receive notifications every time a picture is being 
added to S3.

You should now be able to add a new picture for each shipment. Files that are not pictures will be deleted
and the shipment picture will be replaced with a generic icon.

### Developer environment

To switch to using LocalStack instead of AWS services just run `docker compose up` to spin up a Localstack
container.
After that, the Spring Boot application needs to start using the dev profile:
```
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

This should again populate the DynamoDB, this time on LocalStack.
From here on, the rest of the steps are the same, but all the scripts that need to run end in `-local`,
as they use the `awslocal` CLI.

The same actions should be easily achieved again, but locally.


