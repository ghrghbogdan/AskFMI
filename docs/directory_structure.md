```
/askFMI/
├── .gitignore               
├── README.md                
├── docker-compose.yml       
|
├── frontend/                
│   ├── Dockerfile           
│   ├── package.json
│   ├── public/
│   └── src/
│       ├── components/      
│       ├── pages/           
│       └── services/        
│
├── backend-spring/          
│   ├── Dockerfile           
│   ├── pom.xml              
│   └── src/
│       └── main/
│           ├── java/ro/unibuc/proiect/
│           │   ├── controller/    
│           │   ├── service/       
│           │   ├── repository/    
│           │   ├── model/         
│           │   └── dto/           
│           └── resources/
│               └── application.properties 
│
├── ai-core-fastapi/         
│   ├── Dockerfile           
│   ├── requirements.txt     
│   ├── main.py              
│   ├── rag_pipeline.py      
│   └── vector-store/        
│
├── data-pipeline/
|   ├── Dockerfile           
│   ├── requirements.txt     
│   ├── crawler.py           
│   ├── indexer.py           
│   └── fine_tuning/
│       ├── dataset.jsonl    
│       └── train.py         
│
└── docs/                    
        
```
