# Project Overview

Two-person project where we were tasked in creating a database for a mechanic shop. We were given the initial data to be inserted, a skeleton of the Java interface for the database, and some scripts to help run everything. We had to:

* Implement the Java interface that asks the user for the values to be inserted into the database
* Implement error checking to make sure values fit the domains of the attributes in the database
* Ensure that the SQL queries we created followed the relational schema as per the ER diagram and the specifications
* Demo the project to ensure it worked and complied with the specifications (Which we successfully did)

## Assumptions:

* According to ER diagram, a customer must own at least one car and a car must have exactly one owner. To follow this, we have it so a new customer has to be input with a car as well. Each new car is assigned to a customer
* For VIN, we found out that they can vary in sizes from 11 to 17 characters (https://www.autocheck.com/vehiclehistory/autocheck/en/vinbasics) so we added that functionality
* We assume we can close a request multiple times (in case it needs to be updated to reflect more work done)
* When taking in user input, we assume that none of the entries can be left blank.
* For query 6, we assumed that it was referring to each customer’s bill < 100, and not total customer’s bill < 100.
* We assume mechanics can have 0 years of experience(They could have only a few months of experience or could be a new mechanic)
* We assume requests can be closed on the same day

## Functions

* Add customers: 
    * Enter customer data (with checks to make sure data is valid domain and size for the database)
    * Generates a new ID for customer by getting current max id in table and plus 1
    * Inserts new customer and then call AddCar
* Add mechanic:
    * Adds mechanic info and makes with checks to make sure data is valid domain and * size for the database)
    * Generates a new ID for mechanic by getting current max id in table and plus 1
    * Inserts new mechanic
* Add Car:
    * Adds car info and makes with checks to make sure data is valid domain and size for the database)
    * Generates a new ID for car by getting current max id in table and plus 1
    Inserts Car into database
    * Assign a customer to a car (either existing or a new customer [calls AddCustomer])
* Insert Service Request
    * Finds existing cars on customer last name. If none found, ask to add new car for customer. If more than 1 customer, output options and ask for choice
        * If new car, assigns it to selected customer as Owns
    * Output all cars customer owns and select one to create service request for
   * Generates new ID for service request by getting current max id in table and adding plus 1
* Close Service Request
    * Take user input for an existing service request and and an existing mechanic id
        * If user input is valid, then ask for comment and bill
    * Add to close service request: (newly generated) close request id, service request id, mechanic id, today’s date, comment, and bill
* List customers with bill < 100
    * Look through closed request’s bills and if that number is < 100, store the customer’s name that belongs to that customer’s sid
    * Output the results
* List customers with more than 20 cars
    * For each customer, count the number of car vins they have in the owns relation
    * If the customer sid is connected to more than 20 vins, then store it in a list
    * Output the results
* List cars before 1995 with 50000 miles
    * For each car, check to see if the car’s year is less than 1995
    * Then, check to see if the odometer reading for those cars’ service requests is lower than 50000 miles
    * Output the results
* List k cars with the most services
    * Count the the number of service requests for each vin
    * Order cars based on number of service requests with the car with the most service requests at the top
    * User input for k determines how far you loop through the car table
    * Output the results
* List customers in descending order of their total bill
    * Sum up all of the bills for each customer
    * Order customers based on total bill with the highest bill at the top
    * Output the results

## Edge Cases

* Apostrophes in “complaint” or “comments” causes an error on insert because you need to “escape” them before insert. SQL does NOT like apostrophes in strings

* We assumed mechanic id started with 1, but we were mistaken.

* If the database is empty, we are not sure how MAX(id) + 1 would return when generating a new id. We assumed the table would always have some values
