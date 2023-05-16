variable "env" {
  type        = string
  description = "dev env"
  default = ""
}

variable "sns_sub_endpoint" {
  type        = string
  description = "SNS subscriber endpoint"
  default = "https://localhost:8081/sns/notifications"
}