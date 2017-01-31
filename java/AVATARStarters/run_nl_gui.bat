for /l %%x in (1, 1, 100000) do (
echo %%x
ant run -Dlogback.configurationFile=logconfig.xml -Drun.argline="gui NL"
)