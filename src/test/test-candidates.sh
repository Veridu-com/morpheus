#!/usr/bin/env bash

MODEL=firstname-candidates

curl --insecure -X POST -H "Content-Type: application/json" -H "Authorization: Basic Y2Fzc2lvOmdvZA==" -H "Cache-Control: no-cache" -H "Postman-Token: f9d99d16-c9df-76ed-4491-4c55ebc32b2e" -d '{
	"publicKey": "4c9184f37cff01bcdc32dc486ec36961",
	"userName": "f67b96dcf96b49d713a520ce9f54053c",
	"processId": 1,
	"sourceId": 1,
	"verbose": true
}' "https://localhost:8080/morpheus/${MODEL}"

