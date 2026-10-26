using { task2.db as db } from '../db/schema';

service VehicleService {

    @cds.redirection.target
    entity Vehicles as projection on db.Vehicles;
    entity vehicleView as projection on db.vehicleView;

    @cds.redirection.target
    entity States as projection on db.States;
    entity stateView as projection on db.stateView;

    entity Dealers as projection on db.Dealers;
    entity Orders as projection on db.Orders;
    entity Customers as projection on db.Customers;
    entity Delivery as projection on db.Delivery;

}