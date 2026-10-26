namespace oms;

entity ApiLog {
    key ID          : UUID;
    userId          : String(100);
    userName        : String(100);
    event           : String(20);
    entityName      : String(100);
    timestamp       : Timestamp;
}