// using {my.carshowroom as mcs} from '../db/showroom-schema';
// using { cuid} from '@sap/cds/common';

// service CarShowroomService @(requires : 'authenticated-user') {

//     entity Manufacturers @(restrict:[
//         { grant: ['READ','CREATE','UPDATE','DELETE'], to: 'admin' }
//         ]) as projection on mcs.Manufacturer;

//     entity CarModels @(restrict : [
//         { grant : 'READ', to :[ 'admin','dealer']},
//         { grant : ['CREATE','UPDATE','DELETE'], to : 'admin'}
//     ]) as projection on mcs.CarModel;

//     entity Cars @(restrict : [
//         { grant : 'READ', to : ['admin', 'dealer', 'customer']},
//         { grant : 'UPDATE', to : 'customer'},
//         { grant : 'DELETE', to : 'admin'}
//     ]) as projection on mcs.Car;

//     entity CarsView as select from mcs.Car {
//         ID
//     };

//     entity Showrooms @(restrict : [
//         { grant : 'READ', to : ['admin','dealer']},
//         { grant : ['CREATE', 'UPDATE', 'DELETE'], to : 'dealer'}
//     ]) as projection on mcs.Showroom;

//     entity Orders1 @(restrict : [
//         { grant : 'READ', to : ['admin', 'dealer']},
//         { grant : ['CREATE', 'UPDATE', 'DELETE'], to : 'dealer'}
//     ])as projection on mcs.Order;
    
//     @cds.redirection.target
//     entity StateTaxes @(restrict : [
//         { grant: ['READ','CREATE','UPDATE','DELETE'], to: 'admin' }
//     ])as projection on mcs.StateTax;

//     entity StateView as select from mcs.StateTax{
//         state
//     };

//     entity CarModelShowrooms as projection on mcs.CarModelShowroom;
    
//     @cds.persistence.table
//     entity UserLogs @(restrict : [
//         { grant : 'READ', to : 'admin'},
//         { grant:'CREATE', to:'authenticated-user'}
//     ]) : cuid {
//       userID      : String;
//       entityName  : String;
//       action      : String;
//       createdAt   : Timestamp;
//   };








//     // entity Orders as projection on mcs.Order actions {
        
//     //     action approveOrder() returns String;
//     //     action cancelOrder () returns String;

//     // };
   
//     // action orderCars ( carModel_ID : UUID,showroom_ID : UUID, quantity : Integer) returns String;

//     // function calculateTax ( orderID : UUID) returns Decimal(15,2);


// }

//  "users": {
//           "admin": {
//             "roles": ["admin"]
//           },
//           "dealer": {
//             "roles": ["dealer"]
//           },
//           "customer": {
//             "roles": ["customer"]