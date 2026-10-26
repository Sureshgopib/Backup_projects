// Dealer-1:dealer123
// headers: Basic RGVhbGVyLTE6ZGVhbGVyMTIz


// Admin:admin123
// headers: Basic QWRtaW46YWRtaW4xMjM=




//     this.on('orderCars',async (req) => {

//     const { carModel_ID, showroom_ID, quantity} = req.data;

//     const carmodel = await SELECT.one.from(CarModels).where({ ID : carModel_ID });
//     if(!carmodel) {
//         req.error (400,"Carmodel not found")
//     }

//     const showroom = await SELECT.one.from(Showrooms).where({ ID: showroom_ID});
//     if(!showroom) {
//         req.error (400,"Showroom not found")
//     }

//     if(carmodel.stock < quantity)
//         return req.error(400,"Not enough stock");

//     await INSERT.into(Orders).entries({
//         quantity: quantity,
//         showroom_ID: showroom_ID,
//         carModel_ID: carModel_ID,
//         status: 'pending'
//     });

//     return 'Order created Successfully'

//    })
    


    // this.on('approveOrder',async (req) => {

    // const  orderID  = req.params[0].ID

    // const orders = await SELECT.one.from(Orders)
    //     .where({ ID: orderID })

    // if(!orders) return req.error(404,'Order not found'); 

    // if( orders.status === 'cancelled'){
    //     req.error(400,'Cannot approve the Cancelled order')
    // };

    // const showroom = await SELECT.one.from(Showrooms).where({ ID: orders.showroom_ID });

    // const stateData = await SELECT.one.from(StateTaxes).where({ state : showroom.state_state });

    // const model = await SELECT.one.from(CarModels).where({ ID: orders.carModel_ID });

    // // reduce stock after ordered cars
    // await UPDATE(CarModels).set({ stock : model.stock - orders.quantity }).where({ ID : model.ID });

    // for ( let i=0; i < orders.quantity; i++){

    // // autogenerate CAR Id based on state of showroom 
    // const carID = stateData.stateSF + '-' + cds.utils.uuid().slice(0,4);


    // // const random = Math.floor(1000+ Math.random()*9000)
    // // const carID = showroom.state + '-' + random;
    // // console.log(carID);
    

    //     await INSERT.into(Cars).entries({
    //         ID : carID,
    //         model_ID : model.ID,
    //         showroom_ID : showroom.ID,
    //         name : model.name,
    //         price : model.baseprice,
    //         status : 'available'
    //     });
    //     }
    
    // await UPDATE(Orders).set({ status : 'approved' }).where({ ID : orderID });

    // return "Order approved and cars generated"

    // });



    // this.on('cancelOrder', async (req)=> {

    //     const orderID = req.params[0].ID;

    //     const order = await SELECT.one.from(Orders).where({ ID : orderID});
    //     // const carmodel = await SELECT.one.from(CarModels).where({ ID :order.carModel_ID });

    //     if(!order) {
    //         req.error(400,'Order nor exists');
    //     };

    //     if( order.status === 'approved') {
    //         req.error( 400, 'Approved order cannot be cancelled !!!')
    //     };
        
    //     await UPDATE(Orders).set({ status : 'cancelled'}).where({ ID : orderID});

    //     return " Order cancelled"

    // });


    //  this.on('calculateTax', async (req) => {

    //     const {orderID} = req.data;

    //     if(!orderID) return req.error(400,"OrderID required");
        
    //     const order = await SELECT.one.from(Orders).where({ ID : orderID });
    //     if(order.status === "pending" && "cancelled"){
    //         req.error (400,'Tax cannot be calculated')
    //     };

    //     const showroom = await SELECT.one.from(Showrooms).where({ ID : order.showroom_ID});

    //     const statedata = await SELECT.one.from(StateTaxes).where({state : showroom.state_state});
    //     if(!statedata.state) return req.error(400,"state is required");
    //     if(!statedata.stateSF) return req.error(400, "Tax not registered for State");

    //     const model = await SELECT.one.from(CarModels).where({ ID : order.carModel_ID});

    //     // tax calculation:
    //     const baseAmount = model.baseprice * order.quantity;

    //     const tax = (statedata.taxRate * baseAmount) / 100;

    //     const totalAmount = baseAmount + tax;
        
    //     await UPDATE(Orders).set({
    //         totalPrice : totalAmount,
    //         taxAmount : tax
    //     }).where({ ID : orderID });

    //     return `total Amount including tax: ${totalAmount}`;
    
    //  });


    //  this.after('calculateTax', async (totalAmount, req) => {
    //     req.info(`Tax Calculated and order updated :  ${totalAmount}`)
    //  });