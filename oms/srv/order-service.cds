using {oms as db} from '../db/order';



service OrderService {

entity Orders as projection on db.Orders;
entity OrderItems as projection on db.OrderItems;

@cds.redirection.target
entity orderitemsduplicate as projection on db.OrderItems;

action saveorder(orders: Orders) returns String;

action patchOrder() returns String;

function calculateTotal()  returns Decimal(15,2);

}