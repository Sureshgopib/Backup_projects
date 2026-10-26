const cds = require('@sap/cds');

module.exports = cds.service.impl(async function () {

    const { Vehicles, Orders, Dealers, States } = this.entities;


    this.before('CREATE', '*', async (req) => {
        
        const data = req.data;

        for (const field in data) {

            if (data[field] === null || data[field] === undefined || data[field] < 0) {
                req.error(400, `Field ${field} cannot be empty`);
            }

        }

    });

    //creating records in entity dynamically
    this.on('CREATE', '*', async (req) => {
        
        const entity = req.target.name;
        const data = req.data;
        const tc = cds.tx(req);

        if(entity === 'VehicleService.Vehicles') { //updating vehicle id

            const updatedData = await getUpdatedVehicleI(data);
            await tc.run(INSERT.into(entity).entries(updatedData));
            return updatedData;    
        }
        await tc.run(INSERT.into(entity).entries(data));
        return data;
    });

    
    //vehicle id genrating
     async function getUpdatedVehicleI(vehicle){
        
        let updatedVechicleId = "";
        const dealerId = vehicle.dealer_dealerId;
        let ispresented = true;
        let randomId = 0;

        while(ispresented){
            
            randomId = Math.round(Math.random() * (9999 - 1000) + 1000);
            ispresented = await isRepeated(randomId);

        }

        const dealer = await SELECT.one.from(Dealers).where({ dealerId });
        if(!dealer) req.error(400, `No dealer found for ${dealerId}`);
        const stateId = dealer.stateId;
        const stateInfo = await SELECT.one.from(States).where({ stateId });
        if(!stateInfo) req.error(400, `No state found for ${stateId}`);

        updatedVechicleId += stateInfo.stateCode;
        updatedVechicleId += randomId;
        vehicle.vehicleId = updatedVechicleId;
        
        return vehicle;
    }

    //prevent duplicate vehicle id
    async function isRepeated(random) {
        console.log('isRepeated is called');
        
        const data = await SELECT.one.from(Vehicles).where(`vehicleId like '%${random}'`);   
        return;   
    }
    
    this.on('UPDATE', Vehicles,async (req) => {
        
        const { vehicleId, cur_price } = req.data;

        const newPrice = cur_price;
        
        const vehicleData = await SELECT.one.from(Vehicles).where({ vehicleId });
        if(!vehicleData) req.error(400, `No vehicle found ${ vehicleId } to update`);
        
        const currentPrice = vehicleData.cur_price;
        
        await UPDATE(Vehicles)
            .set({ cur_price    : newPrice,
                    old_price   : currentPrice
             })
            .where({ vehicleId });

        return `Vehicle ${vehicleId} price ${cur_price} updated successfully`;
    })

});