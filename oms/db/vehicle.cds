namespace vehicle;

using {cuid} from '@sap/cds/common';

entity state
{
  key stateId:String(2);
  stateName:String(20);

}


entity VehicleModel
{
    key modelId:String(20);
    modelName:String(25);
    manufacturer:String(25);
    fuelType:String(15);
    
    vehicles:Association to many Vehicle on vehicles.model=$self;
}


entity  Vehicle
{
    key vehicleId:String(20);
    vehicleName:String(30);
    oldPrice:Decimal(15,2,);
    newPrice:Decimal(15,2);
    status:String(20);
    state:Association to state;
    model:Association to VehicleModel;
    relatedVehicle : Association to Vehicle
}


entity Customer:cuid
{
    customerId:UUID;
    customerName:String(30);
    email:String(40);
    phone:String(15);
    address:String(200);
    orders:Composition of many VehicleOrder on orders.customer=$self;
}


entity VehicleOrder
{
    key OrderId:UUID;
    orderNumber:String(20);
    orderDate:Date;
    status:String(20);
    totalAmount:Decimal(15,2);
    customer:Association to Customer;
    items:Composition of many OrderItem on items.order=$self;
}


entity OrderItem
{
    key itemId:UUID;
    quantity:Int32;
    unitPrice:Decimal(15,2);
    totalPrice:Decimal(15,2);
    order:Association  to VehicleOrder;
    vehicle:Association to Vehicle;
}