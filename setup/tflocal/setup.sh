
tflocal init
tflocal plan -var 'env=dev'
tflocal apply -var 'env=dev' --auto-approve
