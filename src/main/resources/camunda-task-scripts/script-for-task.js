var user1 = {
    id: 1,
    name: 'John Doe'
}
var user2 = {
    id: 2,
    name: 'Jane Smith'
}

print('[Service_for_Script] Hello - I am a script');
print('[Service_for_Script] Token Variable: ' + execution.getVariable("tokenVariable"));
print('[Service_for_Script] Status: ' + execution.getVariable("tokenVariable").getStatus());
print('[Service_for_Script] Business key:' + execution.processBusinessKey);

print("user :: " + JSON.stringify(user1));

execution.setVariable("User02", JSON.stringify(user2));

JSON.stringify(user1); // we are returning the user object. In the Model we return this as User01