# AlphaCab
Test assignment for Phonepay

Alpha Cab Management

Used Springboot application with sqlite db for data simulation and processing

Restful APis to interact between interfaces and backend

Note before starting:

1.	The ideal architecture of the system should be a streaming kafka for interctions.
2.	API based requests systems but only for data exchange, all assign transacrtions to be handled using kafka streaming
3.	Data layer to be used will be a mix of sql and no sql database. All current live actions of the users will be on no sql and hard data that does not change will be on no sql
4.	There is a local sqlite database inside the project with some sample data.
5.	It also contains a Reports table that can be used to extend and create reports as pe the problem statement. – All required reports can be easily extracted from the table.
6.	There will be a lot of improvements once the architecture decisions are taken with microservice and orchestration mechanism to handle high transactions systems


Endpoints

All the APIs provide interfaces to the application for access

For Admin
•	localhost:8080/getallvehicles – To list all vehicles
•	localhost:8080/registerVehicle – To register a new vehicle
•	localhost:8080/changeCity – Change city for a CAB – Only works when CAB is IDLE and in service
 
For Users
•	localhost:8080/getavailable – Lists the best CAB available to the USER. Sorts by the MOST IDLE cab in that city
•	localhost:8080/bookcab – Books the available cab – Not explicity for the user but for the application that will allow user to book
•	localhost:8080/endtrip – End Trip for the user
 


