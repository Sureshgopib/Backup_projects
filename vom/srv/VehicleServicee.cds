using {com.vehicle.b2b as db} from '../db/vehicleordermanage';
 
@path: '/vehicle-management'
service VehicleManagementService {
 
    @readonly
    entity States as projection on db.States {
        ID,
        stateCode,
        stateId

    };
 
    // entity Manufacturers as projection on db.Manufacturers {
    //     ID,
    //     manufacturerCode,
    //     manufacturerName,
    //     email,
    //     phone,
    //     address,
    //     isActive,
    //     state,
    //     vehicles,
    //     dealers
    // };
 
    // entity Dealers as projection on db.Dealers {
    //     ID,
    //     dealerCode,
    //     dealerName,
    //     email,
    //     phone,
    //     address,
    //     isActive,
    //     state,
    //     manufacturer,
    //     orders
    // };
 
    @readonly
    entity Vehicles as projection on db.Vehicles {
        ID,
        vehicleId,
        modelCode,
        newPrice,
        oldPrice,
        priceDifference
    };
 
    entity VehicleOrders as projection on db.VehicleOrders {
        ID,
        orderNumber,
        orderDate,
        status,
        totalAmount,
        currency,
        remarks,
        manufacturer,
        dealer,
        items
    };
 
    entity VehicleOrderItems as projection on db.VehicleOrderItems {
        ID,
        quantity,
        unitPrice,
        totalPrice,
        status,
        order,
        vehicle
    };
 
    @readonly
    entity APIUserLogs as projection on db.APIUserLogs {
        ID,
        apiName,
        httpMethod,
        entityName,
        action,
        userId,
        userName,
        requestTime,
        statusCode
    };



    action createOrder(
        orderNumber  : String(20),
        orderDate    : Date,
        manufacturerId  : UUID,
        dealerId        : UUID,
        status       : String(20),
        totalAmount  : Decimal(15,2),
        currency     : String(3),
        remarks      : String(255),
    ) returns VehicleOrders;
 

  function readOrder(
        orderId : UUID
    ) returns VehicleOrders;


      action updateOrder(
        orderId      : UUID,
        orderNumber  : String(20),
        orderDate    : Date,
        manufacturerId  : UUID,
        dealerId        : UUID,
        status       : String(20),
        totalAmount  : Decimal(15,2),
        currency     : String(3),
        remarks      : String(255)
    ) returns VehicleOrders;



    action deleteOrder(
        orderId : UUID
    ) returns String;


    action submitOrder(
        orderId : UUID
    ) returns String;
 
    action approveOrder(
        orderId : UUID
    ) returns String;
 
    action rejectOrder(
        orderId : UUID,
        reason : String
    ) returns String;
 
    action updateVehiclePrice(
        vehicleId : String,
        newPrice : Decimal(15,2)
    ) returns String;
}