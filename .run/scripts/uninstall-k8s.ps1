cd target/helm/repo

$file = Get-ChildItem -Filter workflow-cib7-hello-world-v*.tgz | Select-Object -First 1
$APPLICATION_NAME = Get-ChildItem -Directory | Where-Object { $_.LastWriteTime -ge $file.LastWriteTime } | Select-Object -ExpandProperty Name

helm uninstall $APPLICATION_NAME --namespace workflow-cib7-hello-world
