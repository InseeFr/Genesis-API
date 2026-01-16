#!/bin/bash

MONGODB_INITIAL_PRIMARY_HOST=mongod1
#port=${PORT:-27017}
MONGODB_PORT_NUMBER=27017

echo "###### Waiting for ${MONGODB_INITIAL_PRIMARY_HOST} instance startup.."
until mongosh --host ${MONGODB_INITIAL_PRIMARY_HOST}:${MONGODB_PORT_NUMBER} --eval 'quit(db.runCommand({ ping: 1 }).ok ? 0 : 2)' &>/dev/null; do
  printf '.'
  sleep 1
done
echo "###### Working ${MONGODB_INITIAL_PRIMARY_HOST} instance found, initiating user setup & initializing rs setup.."

# setup user + pass and initialize replica sets
mongosh -u $MONGO_INITDB_ROOT_USERNAME -p $MONGO_INITDB_ROOT_PASSWORD --host ${MONGODB_INITIAL_PRIMARY_HOST}:${MONGODB_PORT_NUMBER} <<EOF

var config = {
    "_id": "dbrs",
    "version": 1,
    "members": [
        {
            "_id": 1,
            "host": "${MONGODB_INITIAL_PRIMARY_HOST}:${MONGODB_PORT_NUMBER}",
            "priority": 2
        }
    ]
};

rs.initiate(config, { force: true });

print("Waiting for node to become PRIMARY...");
const deadline = Date.now() + 60_000; // 60s
while (Date.now() < deadline) {
  try {
    const hello = db.adminCommand({ hello: 1 }); // mongosh recent
    // fallback possible: isMaster on older versions
    if (hello.isWritablePrimary === true) {
      print("Node is PRIMARY ✅");
      break;
    }
  } catch (e) {
    // ignore transient errors during election
  }
  sleep(1000);
}
const finalHello = db.adminCommand({ hello: 1 });
if (finalHello.isWritablePrimary !== true) {
  throw new Error("Timeout: node did not become PRIMARY in time");
}

rs.status();

use genesisdb;

db.createUser(
  {
    user: "genesisuser",
    pwd:  "genesispassword",
    roles: [ { role: "readWrite", db: "genesisdb" } ]
  }
)

EOF
