var user1 = {
    id: 1,
    name: 'John Doe'
}
var user2 = {
    id: 2,
    name: 'Jane Smith'
}

var LoggerFactory = Java.type('org.slf4j.LoggerFactory');
var log = LoggerFactory.getLogger('com.yourcompany.camunda.scripts.ServiceForScript');

log.info('[Service_for_Script] Hello - I am a script');

log.info('[Service_for_Script] Token Variable: ' + execution.getVariable("tokenVariable"));
log.info('[Service_for_Script] Status: ' + execution.getVariable("tokenVariable").getStatus());
log.info('[Service_for_Script] Business key:' + execution.processBusinessKey);

log.info("user :: " + JSON.stringify(user1));

execution.setVariable("User02", JSON.stringify(user2));

JSON.stringify(user1); // we are returning the user object. In the Model we return this as User01