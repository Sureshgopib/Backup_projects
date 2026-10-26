using {my.carshowroom as mcs} from '../db/showroom-schema';
using { cuid} from '@sap/cds/common';

service CarShowroomService {

    entity Manufacturers as projection on mcs.Manufacturer;
    entity CarModels as projection on mcs.CarModel;
    entity Cars as projection on mcs.Car;
    entity CarsView as select from mcs.Car {
        ID
    };
    entity Showrooms as projection on mcs.Showroom;
    entity Orders1 as projection on mcs.Order;
    entity Shipments as projection on mcs.Shipment;
    
    @cds.redirection.target
    entity StateTaxes as projection on mcs.StateTax;
    entity StateView as select from mcs.StateTax{
        state
    };
    entity CarModelShowrooms as projection on mcs.CarModelShowroom;
    
    @cds.persistence.table
    entity UserLogs : cuid {
      userID      : String;
      entityName  : String;
      action      : String;
      createdAt   : Timestamp;
  }








    // entity Orders as projection on mcs.Order actions {
        
    //     action approveOrder() returns String;
    //     action cancelOrder () returns String;

    // };
   
    // action orderCars ( carModel_ID : UUID,showroom_ID : UUID, quantity : Integer) returns String;

    // function calculateTax ( orderID : UUID) returns Decimal(15,2);


}