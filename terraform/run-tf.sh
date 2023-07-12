apt-get update
apt-get install -y wget unzip
wget https://releases.hashicorp.com/terraform/0.14.7/terraform_0.14.7_linux_amd64.zip
unzip terraform_0.14.7_linux_amd64.zip
mv terraform /usr/local/bin/

terraform --version

pip install terraform-local

tflocal init
tflocal plan
tflocal apply --auto-approve