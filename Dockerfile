# Java 21 の環境を使います
FROM amazoncorretto:21

# 作業フォルダを設定
WORKDIR /app

# ファイルをすべてコピー
COPY . .

# Windowsで作った mvnw に実行権限を与える（これ重要！）
RUN chmod +x mvnw

# ビルド実行（テストはスキップ）
RUN ./mvnw clean package -DskipTests

# ポート8080を開放
EXPOSE 8080

# アプリを起動（jarファイルの名前をワイルドカードで指定）
CMD java -jar target/*.jar