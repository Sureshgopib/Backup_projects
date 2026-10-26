
DROP VIEW IF EXISTS VehicleService_apilogs;
DROP VIEW IF EXISTS VehicleService_state;
DROP VIEW IF EXISTS VehicleService_vehicle;
DROP TABLE IF EXISTS cds_outbox_Messages;
DROP TABLE IF EXISTS vehicle_OrderItem;
DROP TABLE IF EXISTS vehicle_VehicleOrder;
DROP TABLE IF EXISTS vehicle_Customer;
DROP TABLE IF EXISTS oms_ApiLog;
DROP TABLE IF EXISTS vehicle_VehicleModel;
DROP TABLE IF EXISTS vehicle_state;
DROP TABLE IF EXISTS vehicle_Vehicle;

CREATE TABLE vehicle_Vehicle (
  vehicleId NVARCHAR(20) NOT NULL,
  vehicleName NVARCHAR(30),
  oldPrice DECIMAL(15, 2),
  newPrice DECIMAL(15, 2),
  status NVARCHAR(20),
  state_stateId NVARCHAR(2),
  model_modelId NVARCHAR(20),
  PRIMARY KEY(vehicleId)
);

CREATE TABLE vehicle_state (
  stateId NVARCHAR(2) NOT NULL,
  stateName NVARCHAR(20),
  PRIMARY KEY(stateId)
);

CREATE TABLE vehicle_VehicleModel (
  modelId NVARCHAR(20) NOT NULL,
  modelName NVARCHAR(25),
  manufacturer NVARCHAR(25),
  fuelType NVARCHAR(15),
  PRIMARY KEY(modelId)
);

CREATE TABLE oms_ApiLog (
  ID NVARCHAR(36) NOT NULL,
  userId NVARCHAR(100),
  userName NVARCHAR(100),
  event NVARCHAR(20),
  entityName NVARCHAR(100),
  timestamp TIMESTAMP(7),
  PRIMARY KEY(ID)
);

CREATE TABLE vehicle_Customer (
  ID NVARCHAR(36) NOT NULL,
  customerId NVARCHAR(36),
  customerName NVARCHAR(30),
  email NVARCHAR(40),
  phone NVARCHAR(15),
  address NVARCHAR(200),
  PRIMARY KEY(ID)
);

CREATE TABLE vehicle_VehicleOrder (
  OrderId NVARCHAR(36) NOT NULL,
  orderNumber NVARCHAR(20),
  orderDate DATE,
  status NVARCHAR(20),
  totalAmount DECIMAL(15, 2),
  customer_ID NVARCHAR(36),
  PRIMARY KEY(OrderId)
);

CREATE TABLE vehicle_OrderItem (
  itemId NVARCHAR(36) NOT NULL,
  quantity INTEGER,
  unitPrice DECIMAL(15, 2),
  totalPrice DECIMAL(15, 2),
  order_OrderId NVARCHAR(36),
  vehicle_vehicleId NVARCHAR(20),
  PRIMARY KEY(itemId)
);

CREATE TABLE cds_outbox_Messages (
  ID NVARCHAR(36) NOT NULL,
  timestamp TIMESTAMP(7),
  target NVARCHAR(255),
  msg NCLOB,
  attempts INTEGER DEFAULT 0,
  "PARTITION" INTEGER DEFAULT 0,
  lastError NCLOB,
  lastAttemptTimestamp TIMESTAMP(7),
  status NVARCHAR(23),
  task NVARCHAR(255),
  appid NVARCHAR(255),
  PRIMARY KEY(ID)
);

CREATE VIEW VehicleService_vehicle AS SELECT
  Vehicle_0.vehicleId
FROM vehicle_Vehicle AS Vehicle_0;

CREATE VIEW VehicleService_state AS SELECT
  state_0.stateId
FROM vehicle_state AS state_0;

CREATE VIEW VehicleService_apilogs AS SELECT
  ApiLog_0.ID,
  ApiLog_0.userId,
  ApiLog_0.userName,
  ApiLog_0.event,
  ApiLog_0.entityName,
  ApiLog_0.timestamp
FROM oms_ApiLog AS ApiLog_0;
