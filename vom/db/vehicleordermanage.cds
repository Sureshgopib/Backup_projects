namespace com.vehicle.b2b;
 
using {cuid,managed} from '@sap/cds/common';
 
  
entity States : cuid, managed {
 
 
    stateId         : String(10)  @mandatory;
    stateCode       : String(5)   @mandatory;
    stateName       : String(100) @mandatory;
    isActive        : Boolean default true;
 
    /* Non-self association */
    manufacturers   : Association to many Manufacturers on manufacturers.state = $self;
    dealers         : Association to many Dealers on dealers.state = $self;
}
 
 
entity Manufacturers : cuid, managed {
 
    manufacturerCode    : String(20)  @mandatory;
    manufacturerName    : String(100) @mandatory;
    email               : String(100);
    phone               : String(20);
    address             : String(250);
    isActive            : Boolean default true;
 
    /* Non-self association */
    state               : Association to States;
    /* Composition */
    vehicles            : Composition of many Vehicles  on vehicles.manufacturer = $self;
    dealers             : Association to many Dealers  on dealers.manufacturer = $self;
}
 
 
 
entity Dealers : cuid, managed {
    dealerCode          : String(20)  @mandatory;
    dealerName          : String(100) @mandatory;
    email               : String(100);
    phone               : String(20);
    address             : String(250);
    isActive            : Boolean default true;
    /* Non-self association */
    state               : Association to States;
    manufacturer        : Association to Manufacturers;
    /* Composition */
    orders              : Composition of many VehicleOrders on orders.dealer = $self;
}
 
 

// VEHICLE
 
entity Vehicles : cuid, managed {

    vehicleId           : String(30) @mandatory;
    modelCode           : String(30) @mandatory;
    modelName           : String(100) @mandatory;
    variant             : String(100);
    color               : String(50);
    manufacturingYear   : Integer;

    /* Price management */
    oldPrice             : Decimal(15,2);
    newPrice             : Decimal(15,2);
    currency             : String(3) default 'INR';
    status               : String(30) default 'AVAILABLE';
     virtual priceDifference   : Decimal(15,2);
    /* Non-self association */
    manufacturer         : Association to Manufacturers @mandatory;
    state                : Association to States  @mandatory;
}
 
 
//vehicle order
entity VehicleOrders : cuid, managed {
    orderNumber         : String(30) @mandatory;
    orderDate           : Date;
    status              : String(30) default 'DRAFT';
    totalAmount         : Decimal(15,2);
    currency             : String(3)default 'INR';
    remarks             : String(500);
    manufacturer        : Association to Manufacturers @mandatory;
    dealer              : Association to Dealers @mandatory;
    items               : Composition of many VehicleOrderItems on items.order = $self;
}
 
 
//vehilce orderitem
 
entity VehicleOrderItems : cuid, managed {
    quantity            : Integer @mandatory;
    unitPrice           : Decimal(15,2);
    totalPrice          : Decimal(15,2);
    status              : String(30) default 'OPEN';
    order               : Association to VehicleOrders  @mandatory;
    vehicle             : Association to Vehicles  @mandatory;
}
 

// API USER LOG
 
entity APIUserLogs : cuid {
    apiName             : String(200);
    httpMethod           : String(10);
    entityName           : String(100);
    action               : String(50);
    userId               : String(200);
    userName             : String(200);
    requestTime          : Timestamp;
    statusCode           : Integer;
    requestPayload       : LargeString;
    responsePayload      : LargeString;
}