# AWS 部署摘要（IAM / AWS CLI / ECR）

本檔整理在 AWS 上建立程式專用 IAM 使用者、設定本機 AWS CLI，以及建立 ECR 儲存庫並推送 Docker image 的必要步驟。內容以 macOS + zsh 為範例。

## 前置條件
- 已有 AWS 帳號並可存取 AWS 管理主控台。
- 安裝 Docker（若要建立與推送映像）。

## 1. 在 IAM 建立程式專用使用者
1. 前往 AWS Console → IAM → Users，點「Create user」。
2. 輸入使用者名稱，例如 `my-app-developer`。
3. Access type：勾選「Programmatic access」，不要勾選 Management Console 存取。
4. Permissions：開發初期可暫時附加 `AdministratorAccess`，正式環境請改為最小權限原則。
5. 建立後到使用者頁面，切換到「Security credentials」。

## 2. 建立並儲存 Access Key（非常重要）
1. 在「Access keys」區塊點「Create access key」。
2. 選擇用途（如 CLI），填寫描述（選填），按建立。  
3. 下載 `.csv` 或複製 Access Key ID 與 Secret Access Key（Secret 只會顯示一次）。

安全提醒：不要把金鑰放到原始碼或公共儲存庫。建議使用 Secrets Manager、SSM Parameter Store、或 CI 的 Secret 功能來管理。

## 3. 在本機安裝並設定 AWS CLI（macOS 範例）
安裝（Homebrew）：

```bash
brew install awscli
```

設定：

```bash
aws configure
```

輸入：
- AWS Access Key ID
- AWS Secret Access Key
- Default region name（例如 `ap-northeast-1`）
- Default output format（預設 json）

短期或腳本用途可用環境變數：

```bash
export AWS_ACCESS_KEY_ID=your_access_key_id
export AWS_SECRET_ACCESS_KEY=your_secret_access_key
export AWS_DEFAULT_REGION=ap-northeast-1
```

驗證設定：

```bash
aws sts get-caller-identity
```

若回傳 ARN 與帳戶資訊表示成功。

## 4. 建立 ECR 儲存庫（Console 與 CLI）
- Console：前往 ECR → Create repository，Visibility 選 Private，Repository name 例如 `maestro-java-backend`，按 Create。
- CLI（建立儲存庫）：

```bash
aws ecr create-repository --repository-name maestro-java-backend --region ap-northeast-1
```

重複建立其他服務的儲存庫（如 `python-nlp-service`, `react-frontend`）。

## 5. 建置並推送 Docker Image（範例）
1. 取得 ECR 登入：

```bash
aws ecr get-login-password --region ap-northeast-1 | docker login --username AWS --password-stdin <account>.dkr.ecr.ap-northeast-1.amazonaws.com
```

2. 建置並標記映像：

```bash
# 於專案目錄建立 image
docker build -t maestro-java-backend:latest .
# 標記為 ECR repository URI
docker tag maestro-java-backend:latest <account>.dkr.ecr.ap-northeast-1.amazonaws.com/maestro-java-backend:latest
```

3. 推送到 ECR：

```bash
docker push <account>.dkr.ecr.ap-northeast-1.amazonaws.com/maestro-java-backend:latest
```

注意替換 `<account>` 與 region、repo 名稱為你的實際值。

## 6. VPC 與 NAT（對外連線）
當你的服務部署在私有子網（private subnet）時，若需要對外存取（例如拉取套件、呼叫外部 API、或推/拉 ECR 映像），就需要透過 NAT 或 VPC Endpoint 來讓私有資源能安全地存取外部網路。

重點摘要
- 私有子網沒有對外 IP，無法直接連到 Internet。
- 若要由 private subnet 對外連線，可使用 NAT Gateway（或自行架的 NAT Instance），或針對 AWS 服務使用 VPC Endpoint（避免走 NAT）。

基本架構（常見）
1. Public subnet：掛載 Internet Gateway（IGW），放置 ALB、bastion、或 NAT Gateway。
2. Private subnet：放應用程式／容器，透過 route table 指向 NAT Gateway 做外網出站。
3. Route table（private）加入 0.0.0.0/0 → nat-gateway-id。

NAT Gateway vs NAT Instance
- NAT Gateway：AWS 管理、穩定且高可用，但會產生按小時計費與資料傳輸費用（簡單、推薦用於生產）。
- NAT Instance：自行管理（EC2），費用較低但需處理 HA/維運，較適合預算有限且能維運的人員。

省錢且安全的替代：VPC Endpoint
- 如果只需存取 AWS 服務（S3、ECR、SSM、CloudWatch 等），建議使用 VPC Endpoint（gateway 或 interface），流量不會經過 NAT，能省成本並增加安全性。

快速 AWS CLI 範例
- 分配 EIP 並建立 NAT Gateway：
```bash
# 1) 分配彈性 IP
aws ec2 allocate-address --domain vpc --region ap-northeast-1

# 2) 建立 NAT Gateway（需 public-subnet-id 與 allocation-id）
aws ec2 create-nat-gateway --subnet-id <public-subnet-id> --allocation-id <eip-alloc-id> --region ap-northeast-1

# 3) 在 private route table 新增路由指向 NAT Gateway
aws ec2 create-route --route-table-id <private-rtb-id> --destination-cidr-block 0.0.0.0/0 --nat-gateway-id <nat-gateway-id>
```

- 建立 VPC Endpoint（S3 example, gateway type）：
```bash
aws ec2 create-vpc-endpoint --vpc-id <vpc-id> --service-name com.amazonaws.ap-northeast-1.s3 --route-table-ids <rtb-id>
```
- ECR 相關 Endpoint（interface type，通常需多個 service）：
```bash
aws ec2 create-vpc-endpoint --vpc-id <vpc-id> --service-name com.amazonaws.ap-northeast-1.ecr.api --subnet-ids <subnet-ids> --security-group-ids <sg-id>
# 另外也建立 ecr.dkr、sts、s3 等需要的 endpoint
```

注意事項與建議
- 成本：NAT Gateway 會產生按小時計費與資料傳輸費用，頻繁的大量外網流量可能成本較高，先評估流量模式。  
- 安全：使用 Security Group 與 NACL 控制出入流量，僅允許必要的 outbound target。  
- 最佳實務：若可能，將 CI/CD runner 或 build 任務放在有外網的環境（或使用 VPC Endpoint 拉取 ECR/S3），避免在 private subnet 上大量依賴 NAT。  
- 當需要對外提供服務（public），把入口放在 public subnet（ALB / API Gateway），內部服務仍置於 private subnet。

是否要我把這段放到 `README.md`（專案讀者版）或產生一個 Terraform 範本來自動建立 VPC/public/private/NAT + Endpoint？

## 參考
- AWS IAM 文件：https://docs.aws.amazon.com/iam/
- AWS CLI 文件：https://docs.aws.amazon.com/cli/
- ECR 文件：https://docs.aws.amazon.com/AmazonECR/latest/userguide/what-is-ecr.html


