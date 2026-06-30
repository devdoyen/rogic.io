import os
from diagrams import Diagram, Cluster, Edge
from diagrams.aws.network import Route53, CloudFront, InternetGateway
from diagrams.aws.compute import EC2
from diagrams.aws.storage import S3
from diagrams.aws.database import RDS
from diagrams.aws.management import Cloudwatch
from diagrams.aws.integration import SNS
from diagrams.onprem.client import User
from diagrams.onprem.network import Nginx
from diagrams.onprem.database import Postgresql

# Set output path relative to project root
out_dir = os.path.join("docs", "images")
os.makedirs(out_dir, exist_ok=True)
filename = os.path.join(out_dir, "aws-architecture")

# Diagram properties
graph_attr = {
    "fontsize": "20",
    "bgcolor": "transparent",
    "splines": "ortho"
}

with Diagram(
    name="rogic.io System Architecture",
    show=False,
    filename=filename,
    outformat="png",
    graph_attr=graph_attr
):
    # Clients
    user = User("🌐 Web Browser\n(User Environment)")

    # AWS Edge
    dns = Route53("📍 AWS Route 53\n(DNS)")
    cdn = CloudFront("⚡ Amazon CloudFront\n(CDN)")
    s3 = S3("🪣 Amazon S3\n(rogic-static-assets)")

    # Monitoring / Observability
    with Cluster("📊 Observability & Alerting"):
        cw = Cloudwatch("📝 Amazon CloudWatch\n(Logs)")
        sns = SNS("🔔 AWS SNS\n(Alerts Topic)")
        email = User("📧 Developer Email")
        cw >> Edge(color="firebrick", style="dashed") >> sns >> Edge(color="firebrick", style="dashed") >> email

    # AWS VPC
    with Cluster("☁️ AWS VPC (ap-northeast-2)"):
        igw = InternetGateway("🌐 Internet Gateway")

        with Cluster("🛡️ Public Subnet (Single EC2 Host)"):
            with Cluster("🐳 frontend-net (Docker Bridge)"):
                nginx = Nginx("🕸️ Nginx Proxy\n(Port 80/443)")
                
            spring = EC2("☕ Spring Boot\n(Backend App)")
            
            with Cluster("🐳 backend-net (Isolated Docker Bridge)"):
                db = Postgresql("🐘 PostgreSQL DB\n(Docker Container)")

            nginx >> Edge(label="Port 8080") >> spring >> Edge(label="JPA/JDBC") >> db

    # User flows
    user >> Edge(label="1. DNS Query") >> dns
    user >> Edge(label="2. Static Assets") >> cdn
    cdn >> Edge(style="dashed", label="Cache Origin") >> s3
    
    user >> Edge(label="3. HTTPS API") >> igw >> nginx

    # Observability flow
    spring >> Edge(color="orange", style="dashed", label="Log stream") >> cw
