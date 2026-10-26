const cds = require('@sap/cds');
const { UPDATE, SELECT, INSERT } = require('@sap/cds/lib/ql/cds-ql');

module.exports = cds.service.impl( async function () {

    const { CarModels, Showrooms, Orders1,Shipments, Cars, StateTaxes, Manufacturers,UserLogs} = this.entities;

    this.before('*', async (req) => {
         console.log("USER:", req.user);
         console.log("headers:", req.headers.authorization);
         

    if (!req.target || req.target.name === 'CarShowroomService.UserLogs') return;

    await INSERT.into(UserLogs).entries({
        ID: cds.utils.uuid(),
        userID: req.user.id,
        entityName: req.target.name,
        action: req.event,
        createdAt: new Date()   
    });
  });



    // manufacturer
    this.before('CREATE',Manufacturers, async (req)=> {
        const { name, location} = req.data;
        if (!name) {
            req.error(400,"Name is required");
        }
        if(!location) {
            req.error(400,'Location is required')
        }
        
    });

    this.on('CREATE',Manufacturers, async(req)=> {
        const { name,location} = req.data;
         await INSERT.into(Manufacturers).entries({
            name,
            location
        });

        return `Manufactureres created`
    });

    this.on('READ', Manufacturers, async (req)=>{
        const data = await cds.run(req.query);
        return data;
    })

    this.on('UPDATE', Manufacturers, async (req) => {
        const { ID } = req.data;
        const manufacturer = await SELECT.one.from(Manufacturers).where({ ID });
        
        if (!manufacturer) req.error(400, "Manufacturer not found");

        await UPDATE(Manufacturers).set(req.data).where({ ID });
        return "Manufacturer updated successfully";
    });

    this.before('DELETE', Manufacturers, async req => {
        const cars = await SELECT.one.from(CarModels).where({ manufacturer_ID: req.data.ID });
        
        if(cars)
        req.error(400,"Cannot delete manufacturer with car models");
    });



    // carmodels
    this.before('CREATE', CarModels,  async(req) => {
        const { stock, baseprice} = req.data;

        if (stock < 0) {
            req.error(400,'Stock should be more than 0')
        };
        if (baseprice <0) {
            req.error(400, 'Price should not be negative')
        }
    });

    this.on('CREATE',CarModels,async(req)=>{
        const {name, stock, baseprice, manufacturer_ID }= req.data;

        const manufacturer = await SELECT.one.from(Manufacturers).where({ ID :manufacturer_ID });
        if(!manufacturer) {
            req.error(400,'Manufacturer required')
        }

        const carmodel = await INSERT.into(CarModels).entries({
            name,
            stock,
            baseprice,
            oldprice : 0,
            manufacturer_ID
        });
        // console.log(carmodel);
        return carmodel
    });

    this.on('READ',CarModels, async (req)=>{
        // console.log(req);
        const data = await SELECT.from(CarModels);  //.where({ stock: { '>' : 20}})
        return data;
    })   

    this.on('UPDATE',CarModels, async (req) => {
        const { ID,baseprice } = req.data;

        const data = await SELECT.one.from(CarModels).where({ ID : ID});
        if(!data) return req.error(400,"Car not found")

            return await UPDATE(CarModels).set({ 
            baseprice : baseprice,
            oldprice : data.baseprice
        }).where({ ID : ID});
    });

    this.before('DELETE', CarModels, async (req) => {
        const { ID } = req.data;

        const model = await SELECT.one.from(CarModels).where({ ID });
        if (!model) {
        return req.error(404, "Car model not found");
        }
        const cars = await SELECT.one.from(Cars).where({ model_ID: ID });
        if (cars) {
        req.error(400, "Cannot delete CarModel because cars are already created for this model");
        }
        const orders = await SELECT.one.from(Orders1).where({ carModel_ID: ID });
        if (orders) {
        req.error(400, "Cannot delete CarModel because orders exist for this model");
        }
    });

    this.after('UPDATE', CarModels, (data) => {
        console.log(`Price updated for model ${data.ID}`);
    });



    // showroom
    this.before('CREATE', Showrooms, async(req)=> {
        const  {name,city,state_state,manufacturer_ID}= req.data;

        if(!name) req.error(400,"Showroom name required");
        if(!city) req.error(400,"City required");
        if(!state_state) req.error(400,"State is mandatory!")

        const manufacturer = await SELECT.one.from(Manufacturers).where({ ID : manufacturer_ID});
        if(!manufacturer) req.error (400, "Manufacturer not found");
    });

    this.on('CREATE', Showrooms, async(req)=>{
        const { name, city, state_state, manufacturer_ID} = req.data;
        await INSERT.into(Showrooms).entries({
            name,  
            city,
            state_state ,
            manufacturer_ID,
        });
        return "Showroom added succesfully!"
    });

    this.after('CREATE',Showrooms,async (data,req)=> {
        console.log(`Showroom added for ${data.state_state}`);
    });

    this.on('READ', Showrooms, async (req) => {
        const data = await cds.run(req.query);
        return data;
    });

    this.on('UPDATE', Showrooms, async (req) => {
        const { ID, manufacturer_ID } = req.data;
        const showroom = await SELECT.one.from(Showrooms).where({ ID });
        if (!showroom) return req.error(404, "Showroom not found");
        if (manufacturer_ID) {
            const manufacturer = await SELECT.one.from(Manufacturers).where({ ID: manufacturer_ID });
        if (!manufacturer) {
            return req.error(400, "Manufacturer not found");
        }
        }
        await UPDATE(Showrooms).set(req.data).where({ ID });
        return "Showroom updated successfully";
    });

    this.on('DELETE', Showrooms, async (req) => {
        const { ID } = req.data;
        const showroom = await SELECT.one.from(Showrooms).where({ ID });
        if (!showroom) {
        return req.error(404, "Showroom not found");
        }
        
        const orders = await SELECT.one.from(Orders1).where({ showroom_ID: ID });
        if (orders) {
        return req.error(400, "Cannot delete showroom because orders exist");
        }
        await DELETE.from(Showrooms).where({ ID });
        return "Showroom deleted successfully";
    });


    // create orders:
    this.before('CREATE', Orders1, async (req)=> {

        const { quantity, showroom_ID,carModel_ID} = req.data;
        if(quantity < 0) req.error(400,"Quantity cannot be Negative");

        const carmodel = await SELECT.one.from(CarModels).where({ ID : carModel_ID});
        if(!carmodel) req.error(400,"Car Model not Found");
        if(!showroom_ID) req.error(400,"Showroom id required");
        if(carmodel.stock < quantity) req.error(400,"Stock not enough")
    });

    this.on('CREATE',Orders1, async (req)=> {
        const { quantity, showroom_ID,carModel_ID} = req.data;

        const carmodel = await SELECT.one.from(CarModels).where({ ID : carModel_ID});
        const showroom = await SELECT.one.from(Showrooms).where({ ID : showroom_ID});
        const statedata = await SELECT.one.from(StateTaxes).where({ state : showroom.state_state});
        if(!statedata) req.error(400, "State is mandatory")

        // tax calculation
        const baseAmount = carmodel.baseprice * quantity ;
        const taxamount = (baseAmount * statedata.taxRate) / 100 ;
        const totalAmount = baseAmount + taxamount;

         const order = await INSERT.into(Orders1).entries({
             quantity: quantity,
             totalPrice : totalAmount,
             taxAmount : taxamount,
             showroom_ID: showroom_ID,
             carModel_ID: carModel_ID,
             status: 'pending'
        });
        return order;
    });

    this.on('READ', Orders1, async (req) => {
        const data = await cds.run(req.query);
        return data;
    });

    this.before('UPDATE',Orders1,async (req)=> {
        const { ID, status} = req.data;
        const order = await SELECT.one.from(Orders1).where({ ID : ID });
        if(!order) req.error (400, "Order not exists");
        if(!status){
            req.error(400,"status must be given");
        };
    });

    // update order and create cars:by manufacturer side-admin
    this.on('UPDATE', Orders1, async (req)=> {
        const { ID, status } = req.data;

        const order = await SELECT.one.from(Orders1).where({ ID : ID });
        if(order.status === 'approved')
        req.error(400,"Order already approved");

        if(status === 'cancelled'){
            await UPDATE(Orders1).set({ status: 'cancelled' }).where({ ID });
            return "Order cancelled";
        };
        if(status === 'approved') {
            const showroom = await SELECT.one.from(Showrooms).where({ ID : order.showroom_ID});
            if(!showroom) req.error(400,"shoeroom not found");
            
            const carmodel = await SELECT.one.from(CarModels).where({ ID : order.carModel_ID});
            if(!carmodel) req.error(400,"Car model not found");
            
            const state = await SELECT.one.from(StateTaxes).where({ state : showroom.state_state});
            if(!state) req.error(400,"state not found");
            
            // reduce stock:
            await UPDATE(CarModels).set({
                stock : carmodel.stock - order.quantity 
            }).where({ ID :carmodel.ID });
            
            await UPDATE(Orders1).set({ 
                status: 'approved' 
            }).where({ ID : ID });

           await INSERT.into(Shipments).entries({
            ID: cds.utils.uuid(),
            order_ID: order.ID,
            shipmentDate: new Date(),
            status: 'shipped'
           });
            
            for(let i=0; i < order.quantity; i++ ){
                const carID = state.stateSF + "-" + cds.utils.uuid().slice(0,4);
                await INSERT.into(Cars).entries({
                    ID : carID,
                    model_ID : carmodel.ID,
                    showroom_ID : showroom.ID,
                    name : carmodel.name,
                    price : carmodel.baseprice,
                    oldprice : carmodel.oldprice,
                    status : 'available'
                });
            }
            return "Cars ordered and created and shipment started"
        }
        return "status not valid";
    });

    this.after('UPDATE',Orders1, async (data)=> {
        if(data.status === 'approved') {
            console.log(`Order approved and cars created`);   
        };
    });

    this.on('DELETE', Orders1, async (req) => {
        const { ID } = req.data;
        
        const order = await SELECT.one.from(Orders1).where({ ID });
        if (!order) {
        return req.error(404, "Order not found");
        }
        if (order.status === 'approved') {
        return req.error(400, "Approved orders cannot be deleted");
        }
        
        await DELETE.from(Orders1).where({ ID });
        return "Order deleted successfully";
    });



    this.before('UPDATE', Shipments, async (req) => {
        const { ID } = req.data;

        const shipment = await SELECT.one.from(Shipments).where({ ID });
        if (!shipment) {
        req.error(404, "Shipment not found");
        }
    });
    
    this.on('UPDATE', Shipments, async (req) => {
        const { ID, status } = req.data;

        const shipment = await SELECT.one.from(Shipments).where({ ID });
        if(!shipment) req.error(404,"Shipment not found");

        await UPDATE(Shipments).set({ status }).where({ ID });

        if(status === 'delivered'){
        return "Shipment delivered to showroom";
        }
        return "Shipment updated";
    });

    this.on('READ', Shipments, async (req) => {
    return await cds.run(req.query);
    });

    this.on('DELETE', Shipments, async (req) => {
        const { ID } = req.data;
        
        const shipment = await SELECT.one.from(Shipments).where({ ID });
        if (!shipment) {
        req.error(404, "Shipment not found");
        }
        
        await DELETE.from(Shipments).where({ ID });
        return "Shipment deleted successfully";
    });


    // create cars
    this.on('READ', Cars, async (req) => {
         const cars = await cds.run(req.query);
         return cars;
    });

    this.on('UPDATE', Cars, async (req) => {
        const { ID } = req.data;
        const car = await SELECT.one.from(Cars).where({ ID });
        if (!car) {
        return req.error(404, "Car not found");
        }
        await UPDATE(Cars).set(req.data).where({ ID });
        return "Car updated successfully";
    });

    this.on('DELETE', Cars, async (req) => {
         const { ID } = req.data;
         const car = await SELECT.one.from(Cars).where({ ID });
         if (!car) {
            return req.error(404, "Car not found");
         }
         if (car.status === 'booked') {
            return req.error(400, "booked cars cannot be deleted");
         }
         await DELETE.from(Cars).where({ ID });
         return "Car deleted successfully";
    });
    

})




