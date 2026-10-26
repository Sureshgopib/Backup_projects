package customer.vom.handlers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sap.cds.Result;
import com.sap.cds.Row;
import com.sap.cds.ql.Delete;
import com.sap.cds.ql.Insert;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;

import cds.gen.com.vehicle.b2b.VehicleOrderItems;
import cds.gen.com.vehicle.b2b.VehicleOrders;
import cds.gen.vehiclemanagementservice.SubmitOrderContext;
import cds.gen.vehiclemanagementservice.UpdateOrderContext;
import cds.gen.vehiclemanagementservice.UpdateVehiclePriceContext;
import cds.gen.vehiclemanagementservice.Vehicles;
import cds.gen.vehiclemanagementservice.Vehicles_;
import cds.gen.vehiclemanagementservice.ApproveOrderContext;
import cds.gen.vehiclemanagementservice.CreateOrderContext;
import cds.gen.vehiclemanagementservice.DeleteOrderContext;
import cds.gen.vehiclemanagementservice.ReadOrderContext;
import cds.gen.vehiclemanagementservice.RejectOrderContext;
import cds.gen.vehicle.Vehicle;

@Component
@ServiceName("VehicleManagementService")
public class VehicleManagementServiceHandler implements EventHandler {

    private static final String DB_VEHICLE = "com.vehicle.b2b.Vehicles";
    private static final String DB_STATE = "com.vehicle.b2b.States";
    private static final String DB_ORDER = "com.vehicle.b2b.VehicleOrders";
    private static final Logger LOG = LoggerFactory.getLogger(VehicleManagementServiceHandler.class);
    private final PersistenceService db;

    public VehicleManagementServiceHandler(PersistenceService db) {
        this.db = db;
    }

    // 1. VEHICLE CREATE VALIDATION

    @Before(event = "CREATE", entity = "VehicleManagementService.Vehicles")
    public void validateVehicleCreate(Vehicles vehicle) {

        String vehicleId = vehicle.getVehicleId();

        if (vehicleId == null || vehicleId.isBlank()) {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Vehicle ID is mandatory");
        }

        validateVehicleId(vehicleId);

        validatePrice(vehicle.getNewPrice());

        // If oldPrice is not provided during creation,
        // initialize it with newPrice.
        if (((cds.gen.com.vehicle.b2b.Vehicles) vehicle).getOldPrice() == null &&
        vehicle.getNewPrice() != null)
        {

        vehicle.setNewPrice(vehicle.getNewPrice());
        }
    }

    // 2. VEHICLE UPDATE VALIDATION
    @Before(event = "UPDATE", entity = "VehicleManagementService.Vehicles")
    public void validateVehicleUpdate(Vehicles vehicle) {

        String vehicleId = vehicle.getVehicleId();

        if (vehicleId != null && !vehicleId.isBlank()) {
            validateVehicleId(vehicleId);
        }

         validatePrice(vehicle.getNewPrice());
    }


    // 3. PRICE VALIDATION
    private void validatePrice(BigDecimal price) {

        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Vehicle price cannot be negative");
        }
    }

    // 4. VEHICLE ID VALIDATION

    private void validateVehicleId(String vehicleId) {

        if (vehicleId == null || vehicleId.length() < 3) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Invalid Vehicle ID format");
        }

        /*
         * Expected examples:
         *
         * TN-VEH-0001
         * KA-VEH-0002
         * KL-VEH-0003
         *
         * or
         *
         * TN001
         * KA001
         * KL001
         */

        String stateCode = vehicleId.substring(0, 2).toUpperCase();

        Result result = db.run(
                Select.from(DB_STATE)
                        .columns("stateCode")
                        .where(s -> s.get("stateCode").eq(stateCode)));

        Row state = result.single();

        if (state == null) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Vehicle ID must start with a valid State Code: "
                            + stateCode);
        }
    }

    // 5. UPDATE VEHICLE PRICE
    @On
    public String updateVehiclePrice(UpdateVehiclePriceContext context) {

        LOG.info("the vehicle price will be updated  here");

        try {
            String vehicleId = context.getVehicleId();
            BigDecimal newPrice = context.getNewPrice();

         
            // Validate vehicle ID 

            if (vehicleId == null || vehicleId.isBlank()) {

                throw new ServiceException(
                        ErrorStatuses.BAD_REQUEST,
                        "Vehicle ID is required");
            }

            
            // Validate price
            

            if (newPrice == null) {

                throw new ServiceException(
                        ErrorStatuses.BAD_REQUEST,
                        "New price is required");
            }

            if (newPrice.compareTo(BigDecimal.ZERO) < 0) {

                throw new ServiceException(
                        ErrorStatuses.BAD_REQUEST,
                        "Vehicle price cannot be negative");
            }

            // Find vehicle
            Result result = db.run(
                    Select.from(DB_VEHICLE)
                            .columns(
                                    "ID",
                                    "vehicleId",
                                    "oldPrice",
                                    "newPrice")
                            .where(v -> v.get("vehicleId").eq(vehicleId)));

                            

            Row vehicle = result.single();

            if (vehicle == null) {

                throw new ServiceException(
                        ErrorStatuses.NOT_FOUND,
                        "Vehicle not found: " + vehicleId);
            }

            // Get current price
            BigDecimal currentPrice = (BigDecimal) vehicle.get("newPrice");

            if (currentPrice == null) {
                currentPrice = BigDecimal.ZERO;
            }

            
            // Business rule
            // oldPrice = current newPrice
            // newPrice = incoming newPrice

            db.run(
                    Update.entity(DB_VEHICLE)
                            .data(
                                    Map.of(
                                            "oldPrice", currentPrice,
                                            "newPrice", newPrice))
                            .where(v -> v.get("vehicleId").eq(vehicleId)));

                LOG.info("vehicle id is"+vehicleId);
                LOG.info("Vehicle price updated. Vehicle ID: {}, oldPrice: {}, newPrice: {}",vehicleId,currentPrice,newPrice);
            return "Vehicle price updated successfully";
        } catch (Exception e) {
            LOG.info("Errors: "+e);
            return ""+e;
        }
    }


    // 6. CREATE ORDER VALIDATION
    @Before(event = "CREATE", entity = "VehicleManagementService.VehicleOrders")
    public void validateOrderCreate(VehicleOrders order) {


        // Order number
        if (order.getOrderNumber() == null
                || order.getOrderNumber().isBlank()) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Order Number is mandatory");
        }

        // Dealer

        if (order.getDealer() == null) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Dealer is mandatory");
        }

        
        // Manufacturer
      

        if (order.getManufacturer() == null) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Manufacturer is mandatory");
        }

   
        // Default status
     
        if (order.getStatus() == null
                || order.getStatus().isBlank()) {

            order.setStatus("DRAFT");
        }

       
        // Default currency
        if (order.getCurrency() == null
                || order.getCurrency().isBlank()) {

            order.setCurrency("INR");
        }

  
        // Validate total amount

        if (order.getTotalAmount() != null
                && order.getTotalAmount()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Total amount cannot be negative");
        }
    }

    // 7. ORDER ITEM CREATE VALIDATION
    @Before(event = "CREATE", entity = "VehicleManagementService.VehicleOrderItems")
    public void validateOrderItemCreate(VehicleOrderItems item) {

        
        // Quantity

        if (item.getQuantity() == null
                || item.getQuantity() <= 0) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Quantity must be greater than zero");
        }

        // Vehicle
      

        if (item.getVehicle() == null) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Vehicle is mandatory");
        }

        // Order
        if (item.getOrder() == null) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Order is mandatory");
        }

        // Unit price
        if (item.getUnitPrice() != null
                && item.getUnitPrice()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Unit price cannot be negative");
        }

        // Total price calculation

        if (item.getUnitPrice() != null) {

            BigDecimal totalPrice = item.getUnitPrice()
                    .multiply(
                            BigDecimal.valueOf(
                                    item.getQuantity()));

            item.setTotalPrice(totalPrice);
        }

        // Default status

        if (item.getStatus() == null|| item.getStatus().isBlank()) {
                item.setStatus("PENDING");
        }
    }

    // 8. SUBMIT ORDER

    @On(event = "submitOrder", service = "VehicleManagementService")
    public String submitOrder(SubmitOrderContext context) {

        LOG.info("inside the submitorder method");
try{
        String orderId = context.getOrderId();

        if (orderId == null) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Order ID is required");
        }

        // Find order

        Result result = db.run(
                Select.from(DB_ORDER)
                        .columns(
                                "ID",
                                "status")
                        .where(o -> o.get("ID").eq(orderId)));

        Row order = (Row) result.single();

        if (order == null) {

            throw new ServiceException(
                    ErrorStatuses.NOT_FOUND,
                    "Order not found");
        }

        String status = (String) order.get("status");
        LOG.info(status);

        // Only DRAFT can be submitted

        if (!"DRAFT".equalsIgnoreCase(status)) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Only DRAFT orders can be submitted");
        }

        // Update status

        db.run(
                Update.entity(DB_ORDER)
                        .data(
                                Map.of(
                                        "status",
                                        "SUBMITTED"))
                        .where(o -> o.get("ID").eq(orderId)));

                         LOG.info(status);

                                
        return "Order submitted successfully";
                                }
                                catch(Exception e)
                                {
                                        return "Order submitted successfully"+e;
                                }
    }

  
    // approve order
    @On(event = "approveOrder", service = "VehicleManagementService")
    public String approveOrder(ApproveOrderContext context) {

        try{

        String orderId = context.getOrderId();
        if (orderId == null) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Order ID is required");
        }

        Result result = db.run(
                Select.from(DB_ORDER)
                        .columns(
                                "ID",
                                "status")
                        .where(o -> o.get("ID").eq(orderId)));

        Row order = (Row) result;

        if (order == null) {

            throw new ServiceException(
                    ErrorStatuses.NOT_FOUND,
                    "Order not found");
        }

        String status = (String) order.get("status");

        // Only SUBMITTED orders can be approved

        if (!"SUBMITTED".equalsIgnoreCase(status)) {
                throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Only SUBMITTED orders can be approved");
        }

        db.run(
                Update.entity(DB_ORDER)
                        .data(
                                Map.of(
                                        "status",
                                        "APPROVED"))
                        .where(o -> o.get("ID").eq(orderId)));

        return "Order approved successfully";
                                }
                                catch(Exception e)
                                {
                                        LOG.info("order approed successfully"+e);
                                        return "order approved successfully";
                                }
    }

    // 10. REJECT ORDER
    @On(event = "rejectOrder", service = "VehicleManagementService")
    public String rejectOrder(RejectOrderContext context) {

        String orderId = context.getOrderId();

        String reason = context.getReason();

        if (orderId == null) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Order ID is required");
        }

        if (reason == null || reason.isBlank()) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Rejection reason is mandatory");
        }

        Result result = db.run(
                Select.from(DB_ORDER)
                        .columns(
                                "ID",
                                "status")
                        .where(o -> o.get("ID").eq(orderId)));

        Row order = (Row) result;

        if (order == null) {

            throw new ServiceException(ErrorStatuses.NOT_FOUND,"Order not found");
        }

        String status = (String) order.get("status");

        // Only SUBMITTED orders can be rejected

        if (!"SUBMITTED".equalsIgnoreCase(status)) {

            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Only SUBMITTED orders can be rejected");
        }

        db.run(
                Update.entity(DB_ORDER)
                        .data(
                                Map.of(
                                        "status",
                                        "REJECTED",
                                        "remarks",
                                        reason))
                        .where(o -> o.get("ID").eq(orderId)));

        return "Order rejected successfully";
    }




    @On(event = "createOrder", service = "VehicleManagementService")
    public VehicleOrders createOrder(CreateOrderContext context) {

        LOG.info("An order is being created");

    String orderNumber = context.getOrderNumber();

    if (orderNumber == null || orderNumber.isBlank()) {
        throw new ServiceException(
                ErrorStatuses.BAD_REQUEST,
                "Order Number is mandatory"
        );
    }

    // Generate ID
    UUID orderId = UUID.randomUUID();

    Map<String, Object> data = new HashMap<>();

    data.put("ID", orderId);
    data.put("orderNumber", orderNumber);
    data.put("orderDate", context.getOrderDate());
    data.put("status", "DRAFT");
    data.put("totalAmount", context.getTotalAmount());
    data.put("currency", context.getCurrency());
    data.put("remarks", context.getRemarks());
    data.put("manufacturer_ID", context.getManufacturerId());
    data.put("dealer_ID", context.getDealerId());


    db.run(
            Insert.into(DB_ORDER)
                    .entry(data)
    );

    Result result = db.run(
            Select.from(DB_ORDER)
                    .where(o -> o.get("ID").eq(orderId))
    );

    LOG.info("Order creation is completed");

    return result.single(VehicleOrders.class);
}


@On(event = "readOrder", service = "VehicleManagementService")
public VehicleOrders readOrder(ReadOrderContext context) {

        LOG.info("ORDER DATA IS BEING FETCHED");
    String orderId = context.getOrderId();

    if (orderId == null) {
        throw new ServiceException(
                ErrorStatuses.BAD_REQUEST,
                "Order ID is required"
        );
    }

    Result result = db.run(
            Select.from(DB_ORDER)
                    .where(o -> o.get("ID").eq(orderId))
    );

    Row order = result.single();

    if (order == null) {
        throw new ServiceException(
                ErrorStatuses.NOT_FOUND,
                "Order not found: " + orderId
        );
    }

    LOG.info("data fetched successfully"+order);
    return order.as(VehicleOrders.class);
}


@On(event = "updateOrder", service = "VehicleManagementService")
public VehicleOrders updateOrder(UpdateOrderContext context) {

    LOG.info("the existing order will be updated");
    String orderId = context.getOrderId().toString();

    if (orderId == null) {
        throw new ServiceException(
                ErrorStatuses.BAD_REQUEST,
                "Order ID is required"
        );
    }

    // Check whether order exists
    Result existingResult = db.run(
            Select.from(DB_ORDER)
                    .columns("ID", "status")
                    .where(o -> o.get("ID").eq(orderId))
    );

    Row existingOrder = existingResult.single();

    if (existingOrder == null) {
        throw new ServiceException(
                ErrorStatuses.NOT_FOUND,
                "Order not found: " + orderId
        );
    }

    // Update data
    Map<String, Object> data = new HashMap<>();

    if (context.getOrderNumber() != null) {
        data.put("orderNumber", context.getOrderNumber());
    }

    if (context.getOrderDate() != null) {
        data.put("orderDate", context.getOrderDate());
    }

    if (context.getManufacturerId() != null) {
        data.put("manufacturer_ID", context.getManufacturerId());
    }

    if (context.getDealerId() != null) {
        data.put("dealer_ID", context.getDealerId());
    }

    if (context.getStatus() != null) {
        data.put("status", context.getStatus());
    }

    if (context.getTotalAmount() != null) {
        data.put("totalAmount", context.getTotalAmount());
    }

    if (context.getCurrency() != null) {
        data.put("currency", context.getCurrency());
    }

    if (context.getRemarks() != null) {
        data.put("remarks", context.getRemarks());
    }

    if (data.isEmpty()) {
        throw new ServiceException(
                ErrorStatuses.BAD_REQUEST,
                "No fields provided for update"
        );
    }

    db.run(
            Update.entity(DB_ORDER)
                    .data(data)
                    .where(o -> o.get("ID").eq(orderId))
    );

    // Return updated order
    Result result = db.run(
            Select.from(DB_ORDER)
                    .where(o -> o.get("ID").eq(orderId))
    );

    if (result == null) {
        throw new ServiceException(
                ErrorStatuses.NOT_FOUND,
                "Order updated but could not be retrieved");
    }


    LOG.info("the order has been updated"+result);

    return result.single(VehicleOrders.class);
}


@On(event = "deleteOrder", service = "VehicleManagementService")
public String deleteOrder(DeleteOrderContext context) {


        LOG.info("the placed submitted  order will be deleted");
    String orderId = context.getOrderId();

    if (orderId == null) {
        throw new ServiceException(
                ErrorStatuses.BAD_REQUEST,
                "Order ID is required"
        );
    }

    // Check order exists
    Result result = db.run(
            Select.from(DB_ORDER)
                    .columns("ID", "status")
                    .where(o -> o.get("ID").eq(orderId))
    );

    Row order = result.single();
    LOG.info("the fetched order is"+order);

    if (order == null) {
        throw new ServiceException(
                ErrorStatuses.NOT_FOUND,
                "Order not found: " + orderId
        );
    }


    String status = (String) order.get("status");

    // Business validation
    if (!"DRAFT".equalsIgnoreCase(status)) {
        throw new ServiceException(
                ErrorStatuses.BAD_REQUEST,
                "Only DRAFT orders can be deleted"
        );
    }

    // Delete
    Result delresult=db.run(
            Delete.from(DB_ORDER)
                    .where(o -> o.get("ID").eq(orderId))
    );
    LOG.info("the deleted order is"+delresult);

    return "Order deleted successfully";
}





// 11. CALCULATE VIRTUAL PRICE DIFFERENCE

@After(event = CqnService.EVENT_READ, entity = Vehicles_.CDS_NAME)
public void calculatePriceDifference(List<Vehicle> vehicles) {

    LOG.info("Calculating virtual price difference");

    for (Vehicle vehicle : vehicles) {

        // // Find vehicle
        //     cds.gen.com.vehicle.b2b.Vehicles result = db.run(
        //             Select.from(DB_VEHICLE)
        //                     .columns(
        //                             "ID",
        //                             "vehicleId",
        //                             "oldPrice",
        //                             "newPrice")
        //                     .where(v -> v.get("vehicleId").eq(vehicle.getVehicleId())))
        //                     .first(cds.gen.com.vehicle.b2b.Vehicles.class).orElseThrow();


        BigDecimal oldPrice = vehicle.getOldPrice();
        BigDecimal newPrice = vehicle.getNewPrice();

        if(oldPrice == null || newPrice == null)
                throw new RuntimeException("Prices aren't be null to process");

        // Avoid division by zero
        if (oldPrice.compareTo(BigDecimal.ZERO) == 0) {
            ((Vehicle) vehicle).setPriceDifference(null);
            return;
        }

        // Formula:
        //
        // ((newPrice - oldPrice) / oldPrice) * 100
        //
        // Example:
        // oldPrice = 500000
        // newPrice = 550000
        //
        // ((550000 - 500000) / 500000) * 100
        // = 10.00

        BigDecimal priceDifference = newPrice
                .subtract(oldPrice)
                .divide(oldPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // This is a VIRTUAL field.
        // It is NOT stored in the database.
        //result.setPriceDifference(priceDifference);
        vehicle.setPriceDifference(priceDifference);

        LOG.info(
                "Vehicle ID: {}, oldPrice: {}, newPrice: {}, priceDifference: {}%",
                vehicle.getVehicleId(),
                oldPrice,
                newPrice,
                priceDifference
        );
    }
}

}