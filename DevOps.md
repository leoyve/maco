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

## 6. 建議與安全最佳實務
- 不要使用 root 帳號做日常操作；程式應使用 IAM Role（若在 EC2/ECS/Lambda）或最小權限的 IAM 使用者。  
- 不要把金鑰寫進程式碼或公開儲存庫；使用 Secrets Manager / SSM / CI Secrets。  
- 若金鑰外洩，立即停用或刪除該 Access Key。  
- 在生產環境採用 IAM Role 與臨時憑證（例如透過 STS）以降低風險。

## 參考
- AWS IAM 文件：https://docs.aws.amazon.com/iam/
- AWS CLI 文件：https://docs.aws.amazon.com/cli/
- ECR 文件：https://docs.aws.amazon.com/AmazonECR/latest/userguide/what-is-ecr.html