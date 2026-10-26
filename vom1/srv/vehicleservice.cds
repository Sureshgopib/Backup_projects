using {vehicle as db} from '../db/vehicle';

using {oms as logs} from '../db/ApiLog';




service VehicleService {

    entity vehicle  as projection on db.Vehicle
    {
        vehicleId
    };

    entity state as projection on  db.state
    {
        stateId
    };

    entity apilogs as projection on logs.ApiLog;

}