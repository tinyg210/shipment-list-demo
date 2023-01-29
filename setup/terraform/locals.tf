locals {
  json_data  = file("./data.json")
  tf_data    = jsondecode(local.json_data)
}