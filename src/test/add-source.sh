#!/usr/bin/env bash

#############################################################################################################################3
# para gerar um user token:
# no projeto idos-api entrar em vendor/bin/psysh

# mudar o idos-api/db/seeds/S11ServicesSeed.php para as urls locais, a fim de que o manager entregue os requests
# para meu servidor local

# alem disso, abrir o arquivo idos-manager/app/ProcessDaemon.php, linha 123 e tirar o ssl:// do endereco :)
#############################################################################################################################3

# facebook flavio:
#curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{
#    "name": "facebook",
#    "tags": {
        # "access_token": "EAAEO02ZBeZBwMBAHF5DHSVt7gIUR75zeTlUoJUOFdM6rNUNVWBZCR97GHbFgkskqIe2UKPDIPxQy2WZAAyw4gGZCX3Cllz4WfUU3xnr9jPzvPwbirhAXN26ZAR2E7vfHTsjZA5rFgbKXGaqChU1HlzL"
    # }
# }' "http://127.0.0.1:8000/index.php/1.0/profiles/f67b96dcf96b49d713a520ce9f54053c/sources?userToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0YzkxODRmMzdjZmYwMWJjZGMzMmRjNDg2ZWMzNjk2MSIsInN1YiI6ImY2N2I5NmRjZjk2YjQ5ZDcxM2E1MjBjZTlmNTQwNTNjIn0.9FLvclKoNMGNmebKagJSmg7HbjogbOo-lmhApsNsSsc"


#################### tokens do alvaro:

# curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{
#    "name": "facebook",
#    "tags": {
#         "access_token": "EAAEgK5paa9ABAK8t9oEpmTYo1U69VTBjmmKwoej2czJO7cTaMjBXAbHMxhOkVgxd5tBNks2ZAguO6JVFA0F8ZCtFABcjPYbUtpevms5obuZBGFwaypsZAs7GkOyJO0rVMzzvGVnil4CsCZCdHP8HbBqNyY5dgwZAsZD"
#     }
# }' "http://127.0.0.1:8000/index.php/1.0/profiles/f67b96dcf96b49d713a520ce9f54053c/sources?userToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0YzkxODRmMzdjZmYwMWJjZGMzMmRjNDg2ZWMzNjk2MSIsInN1YiI6ImY2N2I5NmRjZjk2YjQ5ZDcxM2E1MjBjZTlmNTQwNTNjIn0.9FLvclKoNMGNmebKagJSmg7HbjogbOo-lmhApsNsSsc"

# curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{
#    "name": "twitter",
#    "tags": {
#        "access_token": "44512027-HsYT971i2MMtMNSpJR5DTBsF11U1argcOHPRh7ITx",
#        "token_secret": "QZc96smLCnCbQcgl6PQ7GEmU4i8pwRnycpZGva7XQjtZa"
#    }
# }' "http://127.0.0.1:8000/index.php/1.0/profiles/f67b96dcf96b49d713a520ce9f54053c/sources?userToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0YzkxODRmMzdjZmYwMWJjZGMzMmRjNDg2ZWMzNjk2MSIsInN1YiI6ImY2N2I5NmRjZjk2YjQ5ZDcxM2E1MjBjZTlmNTQwNTNjIn0.9FLvclKoNMGNmebKagJSmg7HbjogbOo-lmhApsNsSsc"

# curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{
#    "name": "linkedin",
#    "tags": {
#         "access_token": "AQWhnl6dnJaxBxDhln64kVs00vOCc-V4HNnUuNd5lRXC2yubQYKrTzxEOXHfDgUt5j49dGOxlaK3oTawcHrHq5APU-TlMy5MBdRHdMPDXg2GUAroC-dNg-lT0V9MDWrX0RGqT_1j8ueNstQNx6CFs65Q8ZyUVG0JOg4g_e2XkU_uqfQx1ZY"
#     }
# }' "http://127.0.0.1:8000/index.php/1.0/profiles/f67b96dcf96b49d713a520ce9f54053c/sources?userToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0YzkxODRmMzdjZmYwMWJjZGMzMmRjNDg2ZWMzNjk2MSIsInN1YiI6ImY2N2I5NmRjZjk2YjQ5ZDcxM2E1MjBjZTlmNTQwNTNjIn0.9FLvclKoNMGNmebKagJSmg7HbjogbOo-lmhApsNsSsc"

# curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{
#    "name": "paypal",
#    "tags": {
#         "access_token": "A103.fGSY_tuG3z8ZjQ2f-mlA3hMkNCCKJyoU7bDUTpz4sDdlwFpaNVoIJ_llkEQN9Udk.OIC0yDsnTQIykWEjwd3Lu5MI8W8"
#     }
# }' "http://127.0.0.1:8000/index.php/1.0/profiles/f67b96dcf96b49d713a520ce9f54053c/sources?userToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0YzkxODRmMzdjZmYwMWJjZGMzMmRjNDg2ZWMzNjk2MSIsInN1YiI6ImY2N2I5NmRjZjk2YjQ5ZDcxM2E1MjBjZTlmNTQwNTNjIn0.9FLvclKoNMGNmebKagJSmg7HbjogbOo-lmhApsNsSsc"

# curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{
#    "name": "google",
#    "tags": {
#         "access_token": "ya29.Cj-KA8PvAOTeZ7ZG1QgAFdyDeQ-G5YNn0Tk9AyvcuU2MSNnyMdUQDg0oONPTCbeakHE4LrWMPgmuoiwnk5KBVJk"
#     }
# }' "http://127.0.0.1:8000/index.php/1.0/profiles/f67b96dcf96b49d713a520ce9f54053c/sources?userToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0YzkxODRmMzdjZmYwMWJjZGMzMmRjNDg2ZWMzNjk2MSIsInN1YiI6ImY2N2I5NmRjZjk2YjQ5ZDcxM2E1MjBjZTlmNTQwNTNjIn0.9FLvclKoNMGNmebKagJSmg7HbjogbOo-lmhApsNsSsc"

# curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{
#    "name": "dropbox",
#    "tags": {
#         "access_token": "SlvXWmnyszoAAAAAAACOcDLqakHk-wpjJ3NPWAju7xOGeQamgXzR9ouf_CA5KWe3"
#     }
# }' "http://127.0.0.1:8000/index.php/1.0/profiles/f67b96dcf96b49d713a520ce9f54053c/sources?userToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0YzkxODRmMzdjZmYwMWJjZGMzMmRjNDg2ZWMzNjk2MSIsInN1YiI6ImY2N2I5NmRjZjk2YjQ5ZDcxM2E1MjBjZTlmNTQwNTNjIn0.9FLvclKoNMGNmebKagJSmg7HbjogbOo-lmhApsNsSsc"

# curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{
#    "name": "amazon",
#    "tags": {
#         "access_token": "Atza|IwEBIOF-NuKsEqIdlxK7ZXnLIlGKh4Yp4H47_ycHvilAOVShpydZISN5pK2Bf6-Kr-XdTRGIaY17azsELbjTJ54eThYiO7vZzpi1XEXhrGju12fVctvxbOV-iDckgVqPdraUchO7E8ODUAK8K6izQvRDllKZD6B8MX5Y3y8UNSZA8xCtbiKRjIvpA8Xe_QuvjFQvzCVnnLhxxzAMgRrMROIfTtkXwDepzag78DXjae9EhjfVz05wDzM6hmyYxFEDxsuADxJyxbkcwaHuS-VCMijDReKp0HQQcwNze8psKsV2-ia7g3piyl8kuUPesBg4bzPfIhWsXaEEz6jaa9NcIbbJpF3ssNRBueCVFAwc8x6hGEX2X-3GrKthRDYBNJeqwgUTn-H185qL6FMrK7E_4kBM-JuO7fzmhxjSCuvYNA4MVxgzzcrZJUnFA4RGa0aiaFMf5SeIL8bS390eT3EyFEASB8imboFxkhnkeIVVBz0EY1lzFdscvI8R96kq2P03O0vlO5tudlDvqxUcrYKVPC85RhrZYAG9liZ4CXiA3zDjuXR0lGTxTewy7IqEn47ycvoFG7A"
#     }
# }' "http://127.0.0.1:8000/index.php/1.0/profiles/f67b96dcf96b49d713a520ce9f54053c/sources?userToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0YzkxODRmMzdjZmYwMWJjZGMzMmRjNDg2ZWMzNjk2MSIsInN1YiI6ImY2N2I5NmRjZjk2YjQ5ZDcxM2E1MjBjZTlmNTQwNTNjIn0.9FLvclKoNMGNmebKagJSmg7HbjogbOo-lmhApsNsSsc"

# curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{
#    "name": "spotify",
#    "tags": {
#         "access_token": "BQDM0OjYuIl_QUIxlPVbGzziv2xItbLFextZIGaLg_-zS6V8fS18pNCZeAJj9_dClApVegFw4YFZtpt5jkJWm6ivDMVKkgQTPRaQOGi5Un7tlBO3I5znKYgtr-IA5j1ylpQAmDTgjPGcYLU6LL1TgipKYQ1xXbP3aQxYQ2mKuaVRMvkUqc_TFbXAvoQrOzLOAR77d8RQEAbv"
#     }
# }' "http://127.0.0.1:8000/index.php/1.0/profiles/f67b96dcf96b49d713a520ce9f54053c/sources?userToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0YzkxODRmMzdjZmYwMWJjZGMzMmRjNDg2ZWMzNjk2MSIsInN1YiI6ImY2N2I5NmRjZjk2YjQ5ZDcxM2E1MjBjZTlmNTQwNTNjIn0.9FLvclKoNMGNmebKagJSmg7HbjogbOo-lmhApsNsSsc"

# curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{
#    "name": "yahoo",
#    "tags": {
#         "access_token": "SLahpQOZulE7OC3mt5aP6X16qHZD1FfSv3iYLPaGWO5LGATORoVgyVOuQsEoGLKfyKUzh12EeoHcvDRzhP_2kyMO3THm2A9.vY2oswH4hyOUACI4rmwu9OwmxBI2MfuG2O2MXJUn.tvSVDbhyir6s4hfq0Ij3LZVM2Mv0MFTlOS4.H14wGhHvhKGFVK6h6uUYQgS2guim3Es6JkZ6ZOKHpoquzbq_cfQ4o02Hyw.JjoSzdTwaziD_YmUmxrU9fy2QsaIifHsyn5UVpPDWMgkxFvvNKli1anyKlPuuHA6iWaj2rIuNFNNlwD0fwFABQp.HZgubzfWZu3Fp5TlwMZxW5l_6XqSOIzHOcKsDxB1UKjyp_m7kTDayc4Xs61nvdmRvai0V69On0N2ziAYlyDuAUKrYKdZhmuYNqmJB6BemnB3C_fzY.QY7l1mUNAJuurvczIjunOuo8IIGvN3wfzqpG4CEvmaHVvYmo9zDDJjA6LwfGqhJHxEFPI_RepXB4DZ56Sjn6IRb.F.z0uHt0XuKMFYq7GwrKcw0eqpolCY29g3M4BBPgp0KcYu7L_mrvkOVeiTmh5a1wgCvb8TnRZit8Fa91xpp2KZ_sNyFUXkUNUeFRsLfA.rCkNE_iG4Er5AqGXlzJL2Pt0cbJZjJEMiwNbRYaSrZR1rxUl0wjsgHcDGv4_J4YMjIrmv.g7MWfNu2oXk_icCOdsGMjxLoXTk9vipdvrCZ.1RnSm6CMF2HPOESA1IhoRq3q0znqsHlxNpBW6gJTkA7NUFaH_J9pf96oyol9ZHgWejfjcNafKsbZXi77sshNirHECo0vaSN7vxFDYyIea5J1Y-"
#     }
# }' "http://127.0.0.1:8000/index.php/1.0/profiles/f67b96dcf96b49d713a520ce9f54053c/sources?userToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0YzkxODRmMzdjZmYwMWJjZGMzMmRjNDg2ZWMzNjk2MSIsInN1YiI6ImY2N2I5NmRjZjk2YjQ5ZDcxM2E1MjBjZTlmNTQwNTNjIn0.9FLvclKoNMGNmebKagJSmg7HbjogbOo-lmhApsNsSsc"
