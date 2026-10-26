namespace oms;

using {cuid} from '@sap/cds-dk/node_modules/@sap/cds/common';


entity Orders:cuid{
    ordernumber:String(20);
   orderdate:Date;
   customerName:String(30);
   customerEmail:String(40);
   status:String(20);
   totalAmount:Decimal(15, 2);
   items: Composition of many OrderItems
   on items.order=$self;
}


entity OrderItems : cuid {

    productName:String(20);
    quantity:Int32;
     unitPrice:Decimal(15,2);
    totalPrice:Decimal(15,2);
    order : Association to Orders;
    
}