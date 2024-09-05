
# Test the login endpoint
curl --location 'http://localhost:8080/login' \
--header 'Content-Type: application/json' \
--data '{
    "username": "admin",
    "password": "admin"
}'

# Test order book endpoint
curl --location 'http://localhost:8080/orders/order-book' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvb3JkZXJzIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiYWRtaW4iOiJhZG1pbiIsImV4cCI6MTcyNTQ0NzE0MX0.45swItQTsgWnq0iJ6dg_0XagoZk59OpLzZYGPQpqf8w'

# Test limit order endpoint
curl --location 'http://localhost:8080/orders/limit' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvb3JkZXJzIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiYWRtaW4iOiJhZG1pbiIsImV4cCI6MTcyNTQ0NzE0MX0.45swItQTsgWnq0iJ6dg_0XagoZk59OpLzZYGPQpqf8w' \
--data '{
    "side": "BUY",
    "quantity":"40",
    "price": "100",
    "pair": "BTCUSDC",
    "postOnly": true,
    "customerOrderId": "201",
    "timeInForce": "GTC",
    "allowMargin": "false"
}'

# Test the trade history endpoint
curl --location 'http://localhost:8080/orders/trade-history' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvb3JkZXJzIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiYWRtaW4iOiJhZG1pbiIsImV4cCI6MTcyNTQ0NzE0MX0.45swItQTsgWnq0iJ6dg_0XagoZk59OpLzZYGPQpqf8w'

# Test the open orders endpoint
curl --location 'http://localhost:8080/orders/open' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvb3JkZXJzIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiYWRtaW4iOiJhZG1pbiIsImV4cCI6MTcyNTQ0NzE0MX0.45swItQTsgWnq0iJ6dg_0XagoZk59OpLzZYGPQpqf8w'

# Test the orders endpoint
curl --location 'http://localhost:8080/orders/all' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvb3JkZXJzIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiYWRtaW4iOiJhZG1pbiIsImV4cCI6MTcyNTQ0NzE0MX0.45swItQTsgWnq0iJ6dg_0XagoZk59OpLzZYGPQpqf8w'