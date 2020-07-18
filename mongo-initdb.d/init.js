// db.createUser(
//     {
//         user:'user',
//         pwd:'password',
//         roles:[
//             {
//                 role:'readWrite',
//                 db:'chat'
//             }
//         ]
//     }
// );
db.createCollection("messages",{
    capped:true,
    size:500000,
    max:1000
});
