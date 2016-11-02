#!/usr/bin/env bash

# para gerar um user token:
# no projeto idos-api entrar em vendor/bin/psysh

#curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{
#     "name": "facebook",
#     "tags": {
#         "access_token": "EAAEO02ZBeZBwMBAHF5DHSVt7gIUR75zeTlUoJUOFdM6rNUNVWBZCR97GHbFgkskqIe2UKPDIPxQy2WZAAyw4gGZCX3Cllz4WfUU3xnr9jPzvPwbirhAXN26ZAR2E7vfHTsjZA5rFgbKXGaqChU1HlzL"
#     }
# }' "http://127.0.0.1:8000/index.php/1.0/profiles/f67b96dcf96b49d713a520ce9f54053c/sources?userToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0YzkxODRmMzdjZmYwMWJjZGMzMmRjNDg2ZWMzNjk2MSIsInN1YiI6ImY2N2I5NmRjZjk2YjQ5ZDcxM2E1MjBjZTlmNTQwNTNjIn0.9FLvclKoNMGNmebKagJSmg7HbjogbOo-lmhApsNsSsc"


curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{
   "name": "twitter",
   "tags": {
       "access_token": "44512027-HsYT971i2MMtMNSpJR5DTBsF11U1argcOHPRh7ITx",
       "token_secret": "QZc96smLCnCbQcgl6PQ7GEmU4i8pwRnycpZGva7XQjtZa"
   }
}' "https://api.idos.io/1.0/profiles/f67b96dcf96b49d713a520ce9f54053c/sources?userToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0YzkxODRmMzdjZmYwMWJjZGMzMmRjNDg2ZWMzNjk2MSIsInN1YiI6ImY2N2I5NmRjZjk2YjQ5ZDcxM2E1MjBjZTlmNTQwNTNjIn0.9FLvclKoNMGNmebKagJSmg7HbjogbOo-lmhApsNsSsc"

