package handlers;

import java.beans.EventHandler;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
 
import org.springframework.stereotype.Component;
 
import com.sap.cds.Result;
import com.sap.cds.Row;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.handler.annotations.Parameter;
 
import cds.gen.vehiclemanagementservice.Dealers;
import cds.gen.vehiclemanagementservice.Manufacturers;
import cds.gen.vehiclemanagementservice.UpdateVehiclePriceContext;
import cds.gen.vehiclemanagementservice.VehicleOrders;
import cds.gen.vehiclemanagementservice.VehicleOrderItems;
import cds.gen.vehiclemanagementservice.Vehicles;
 
import com.sap.cds.services.persistence.PersistenceService;


@Component
@ServiceName("VehicleManagementService")
public class VehicleOrderServiceManagement implements EventHandler {
 
    private final PersistenceService db;
 
    public VehicleManagementServiceHandler(PersistenceService db) 
    {
        this.db = db;
    }
 

    // 1. VEHICLE CREATE VALIDATION
    @Before(event = "CREATE", entity = "VehicleManagementService.Vehicles")
    public void validateVehicleCreate(Vehicles vehicle) {
 
        String vehicleId = vehicle.getVehicleId();
 
        if (vehicleId == null || vehicleId.isBlank()) {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Vehicle ID is mandatory"
            );
        }
 
        validateVehicleId(vehicleId);
 
        if (vehicle.getNewPrice() != null && vehicle.getNewPrice().compareTo(BigDecimal.ZERO) < 0)
        {
 
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Vehicle price cannot be negative");
        }
    }
 
 
    // 2. VEHICLE UPDATE VALIDATION
    @Before(event = "UPDATE", entity = "VehicleManagementService.Vehicles")
    public void validateVehicleUpdate(Vehicles vehicle) {
 
        String vehicleId = vehicle.getVehicleId();
 
        if (vehicleId != null) {
            validateVehicleId(vehicleId);
        }
 
        if (vehicle.getNewPrice() != null && vehicle.getNewPrice().compareTo(BigDecimal.ZERO) < 0) 
        {
 
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Vehicle price cannot be negative"
            );
        }
    }
 
 
    // 3. VEHICLE ID VALIDATION
    private void validateVehicleId(String vehicleId) {
 
        /*
         * Expected:
         *
         * TN-VEH-0001
         * KL-VEH-0002
         * KA-VEH-0003
         *
         * First two characters should be a valid State Code.
         */
 
        if (vehicleId.length() < 3) 
        {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Invalid Vehicle ID format"
            );
        }
 
        String stateCode = vehicleId.substring(0, 2).toUpperCase();
 
        Row state = db.run(
                Select.from("com.vehicle.b2b.States")
                        .columns("stateCode")
                        .where(s ->
                                s.get("stateCode").eq(stateCode)
                        )
        ).first();
 
        if (state == null) {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Vehicle ID must start with a valid State Code"
            );
        }
    }
 

    // 4. VEHICLE PRICE UPDATE
    @On(event = "updateVehiclePrice", service = "VehicleManagementService")
    public String updateVehiclePrice(UpdateVehiclePriceContext context)
            {
                String vehicleId = context.getVehicleId();
                BigDecimal newPrice = context.getNewPrice();
        if (vehicleId == null || vehicleId.isBlank()) {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Vehicle ID is required"
            );
        }
 
        if (newPrice == null) {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "New price is required"
            );
        }
 
        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Vehicle price cannot be negative"
            );
        }
 
        Result result = db.run(
                Select.from("com.vehicle.b2b.Vehicles")
                        .columns(
                                "ID",
                                "vehicleId",
                                "oldPrice",
                                "newPrice"
                        )
                        .where(v -> v.get("vehicleId").eq(vehicleId))
        );
 
        Row vehicle = result.first();
 
        if (vehicle == null) 
        {
            throw new ServiceException(
                    ErrorStatuses.NOT_FOUND,
                    "Vehicle not found: " + vehicleId);
        }
 
        BigDecimal currentPrice = vehicle.get("newPrice");
 
        if (currentPrice == null) {
            currentPrice = BigDecimal.ZERO;
        }
 
        /*
         * Business rule:
         *
         * oldPrice = current newPrice
         * newPrice = requested price
         */
 
        db.run(
                Update.entity("com.vehicle.b2b.Vehicles")
                .data(Map.of(
                                "oldPrice", currentPrice,
                                "newPrice", newPrice))
                        .where(v ->
                                v.get("vehicleId").eq(vehicleId)
                        )
        );
 
        return "Vehicle price updated successfully";
    }
 

    // 5. CREATE ORDER VALIDATION
    @Before(event = "CREATE",entity = "VehicleManagementService.VehicleOrders")
    public void validateOrderCreate(VehicleOrders order)
    {
 
        if (order.getOrderNumber() == null || order.getOrderNumber().isBlank()) {
 
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Order Number is mandatory"
            );
        }
 
        if (order.getDealer() == null) 
        {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Dealer is mandatory");
        }
 
        if (order.getManufacturer() == null) {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Manufacturer is mandatory"
            );
        }
 
        if (order.getStatus() == null) {
            order.setStatus("DRAFT");
        }
 
        if (order.getCurrency() == null) {
            order.setCurrency("INR");
        }
    }
 
 
    // 6. ORDER ITEM VALIDATION
 
    @Before(
        event = "CREATE",
        entity = "VehicleManagementService.VehicleOrderItems"
    )
    public void validateOrderItemCreate(VehicleOrderItems item) {
 
        if (item.getQuantity() == null
                || item.getQuantity() <= 0) {
 
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }
 
        if (item.getVehicle() == null) {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Vehicle is mandatory"
            );
        }
 
        if (item.getOrder() == null) {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Order is mandatory"
            );
        }
 
        if (item.getUnitPrice() != null
&& item.getUnitPrice()
                    .compareTo(BigDecimal.ZERO) < 0) {
 
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Unit price cannot be negative"
            );
        }
    }
 
 
    // =========================================================
    // 7. SUBMIT ORDER
    // =========================================================
 
    @On(
        event = "submitOrder",
        service = "VehicleManagementService"
    )
    public String submitOrder(
            @Parameter(name = "orderId") UUID orderId) {
 
        Row order = db.run(
                Select.from("com.vehicle.b2b.VehicleOrders")
                        .columns(
                                "ID",
                                "status"
                        )
                        .where(o ->
                                o.get("ID").eq(orderId)
                        )
        ).first();
 
        if (order == null) {
            throw new ServiceException(
                    ErrorStatuses.NOT_FOUND,
                    "Order not found"
            );
        }
 
        String status = order.get("status");
 
        if (!"DRAFT".equals(status)) {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Only DRAFT orders can be submitted"
            );
        }
 
        db.run(
                Update.entity("com.vehicle.b2b.VehicleOrders")
                        .data(Map.of(
                                "status", "SUBMITTED"
                        ))
                        .where(o ->
                                o.get("ID").eq(orderId)
                        )
        );
 
        return "Order submitted successfully";
    }
 
 
    // 8. APPROVE ORDER
    @On(
        event = "approveOrder",
        service = "VehicleManagementService"
    )
    public String approveOrder(
            @Parameter(name = "orderId") UUID orderId) {
 
        Row order = db.run(
                Select.from("com.vehicle.b2b.VehicleOrders")
                        .columns(
                                "ID",
                                "status"
                        )
                        .where(o ->
                                o.get("ID").eq(orderId)
                        )
        ).first();
 
        if (order == null) {
            throw new ServiceException(
                    ErrorStatuses.NOT_FOUND,
                    "Order not found"
            );
        }
 
        String status = order.get("status");
 
        if (!"SUBMITTED".equals(status)) {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Only SUBMITTED orders can be approved"
            );
        }
 
        db.run(
                Update.entity("com.vehicle.b2b.VehicleOrders")
                        .data(Map.of(
                                "status", "APPROVED"
                        ))
                        .where(o ->
                                o.get("ID").eq(orderId)
                        )
        );
 
        return "Order approved successfully";
    }
 
 
    // 9. REJECT ORDER
    @On(
        event = "rejectOrder",
        service = "VehicleManagementService"
    )
    public String rejectOrder(
            @Parameter(name = "orderId") UUID orderId,
            @Parameter(name = "reason") String reason) {
 
        if (reason == null || reason.isBlank()) {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Rejection reason is mandatory"
            );
        }
 
        Row order = db.run(
                Select.from("com.vehicle.b2b.VehicleOrders")
                        .columns(
                                "ID",
                                "status"
                        )
                        .where(o ->
                                o.get("ID").eq(orderId)
                        )
        ).first();
 
        if (order == null) 
        {
            throw new ServiceException(
                    ErrorStatuses.NOT_FOUND,
                    "Order not found");
        }
 
        String status = order.get("status");
 
        if (!"SUBMITTED".equals(status))
        {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Only SUBMITTED orders can be rejected");
        }
 
        db.run(Update.entity("com.vehicle.b2b.VehicleOrders")
                        .data(Map.of(
                                "status", "REJECTED",
                                "remarks", reason
                        ))
                        .where(o ->
                                o.get("ID").eq(orderId)
                        )
        );
 
        return "Order rejected successfully";
    }
 
 
  
    // 10. AFTER ORDER CREATE
    @After(event = "CREATE", entity = "VehicleManagementService.VehicleOrders")
    public void afterOrderCreate(VehicleOrders order) {
 
        /*
         * You can calculate initial values here.
         *
         * Example:
         *
         * totalAmount = 0
         * status = DRAFT
         */
    }
}

