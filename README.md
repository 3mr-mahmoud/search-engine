# Dragon Ball Search Engine

A comprehensive web search engine built with Java, featuring web crawling, indexing, PageRank algorithm, and a modern Vue.js frontend with Spring Boot backend.

## 🏗️ Architecture

The search engine consists of three main components:

### 1. Core Search Engine (Java)
- **Web Crawler**: Multi-threaded web crawler with robots.txt compliance
- **Indexer**: Text processing with stemming, stop word removal, and TF-IDF scoring
- **PageRank**: Link-based ranking algorithm implementation
- **Query Processor**: Advanced search with phrase matching and ranking

### 2. Backend API (Spring Boot)
- RESTful API endpoints for search functionality
- Search history and suggestions
- Pagination support
- MongoDB integration

### 3. Frontend (Vue.js)
- Modern, responsive web interface
- Dark/light theme toggle
- Search suggestions
- Real-time search results
- Pagination component

## 🚀 Features

### Core Search Engine
- **Multi-threaded Web Crawling**: Efficiently crawls up to 6,000 pages with robots.txt compliance
- **Advanced Indexing**: 
  - Porter stemming algorithm
  - Stop word removal
  - TF-IDF scoring
  - Position-based ranking (title, headings)
  - Meta description extraction
- **PageRank Algorithm**: Link-based ranking for better result relevance
- **Phrase Search**: Support for quoted queries for exact phrase matching
- **Ranking System**: Combines TF-IDF, PageRank, and position-based scoring

### Web Interface
- **Modern UI**: Clean, responsive design with Tailwind CSS
- **Dark/Light Theme**: Toggle between themes
- **Search Suggestions**: Auto-complete based on search history
- **Real-time Search**: Instant results with loading states
- **Pagination**: Navigate through search results
- **Search History**: Track and suggest previous searches

### Backend API
- **RESTful Endpoints**: `/api/search` and `/api/suggestions`
- **Pagination**: Configurable results per page
- **Performance Metrics**: Query execution time tracking
- **CORS Support**: Cross-origin requests enabled

## 🛠️ Technology Stack

### Core Engine
- **Java 11**: Main programming language
- **MongoDB**: Document database for storing crawled pages and index
- **JSoup**: HTML parsing and web scraping
- **Porter Stemmer**: Text stemming algorithm
- **Robots.txt Parser**: Web crawling compliance

### Backend
- **Spring Boot 3.3**: REST API framework
- **Spring Data MongoDB**: Database integration
- **Lombok**: Code generation
- **Java 17**: Runtime environment

### Frontend
- **Vue.js 3**: Progressive JavaScript framework
- **Vite**: Build tool and dev server
- **Tailwind CSS**: Utility-first CSS framework
- **Axios**: HTTP client
- **Vue Router**: Client-side routing

## 📁 Project Structure

```
search-engine/
├── src/main/java/
│   ├── Crawler/           # Web crawling components
│   ├── Indexer/           # Text indexing and processing
│   ├── PageRank/          # PageRank algorithm implementation
│   ├── Searching/         # Query processing and search logic
│   └── DB/               # MongoDB database operations
├── backend/               # Spring Boot REST API
│   └── src/main/java/com/dragonball/backend/
├── WebInterface/          # Vue.js frontend application
│   └── src/
├── stopWords.txt         # Stop words for text processing
├── seed.txt              # Initial URLs for crawling
└── pom.xml              # Maven dependencies
```

## 🚀 Getting Started

### Prerequisites
- Java 11+ (for core engine)
- Java 17+ (for backend)
- Node.js 16+ (for frontend)
- MongoDB 4.4+
- Maven 3.6+

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd search-engine
   ```

2. **Set up MongoDB**
   - Install and start MongoDB
   - Create a database for the search engine

3. **Build the core engine**
   ```bash
   mvn clean compile
   ```

4. **Start the backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

5. **Start the frontend**
   ```bash
   cd WebInterface
   npm install
   npm run dev
   ```

### Configuration

1. **Database Configuration** (`backend/src/main/resources/application.properties`)
   ```properties
   spring.data.mongodb.uri=mongodb://localhost:27017/search_engine
   ```

2. **Crawling Configuration** (`src/main/java/DB/Mongo.java`)
   - `MAX_PAGES`: Maximum pages to crawl (default: 6000)
   - `MORE_PAGES`: Additional pages limit (default: 15000)

## 🔧 Usage

### Running the Crawler
```bash
# Navigate to the core engine directory
cd src/main/java

# Run the crawler
java -cp ".:../../../../target/classes" Crawler.MainCrawl
```

### Running the Indexer
```bash
# After crawling is complete
java -cp ".:../../../../target/classes" Indexer.Main
```

### Running PageRank
```bash
# After indexing is complete
java -cp ".:../../../../target/classes" PageRank.Main
```

### Using the Web Interface
1. Start the backend and frontend servers
2. Open `http://localhost:5173` in your browser
3. Enter search queries to find relevant pages
4. Use quotes for exact phrase matching: `"exact phrase"`

## 📊 Search Features

### Query Processing
- **Tokenization**: Splits queries into individual words
- **Stemming**: Reduces words to their root form using Porter algorithm
- **Stop Word Removal**: Filters out common words
- **Case Insensitive**: Handles mixed case queries

### Ranking Algorithm
The search engine uses a combination of:
1. **TF-IDF Score**: Term frequency-inverse document frequency
2. **PageRank Score**: Link-based importance
3. **Position Bonus**: Extra weight for words in titles and headings
4. **Phrase Matching**: Exact phrase queries get higher scores

### Search Types
- **Regular Search**: Find pages containing any of the query terms
- **Phrase Search**: Use quotes for exact phrase matching
- **Suggestions**: Auto-complete based on search history

## 🔍 API Endpoints

### Search API
```
GET /api/search?keyword={query}&page={page}
```
Returns paginated search results with metadata.

### Suggestions API
```
GET /api/suggestions?keyword={partial}
```
Returns search suggestions based on history.

## 🎯 Performance

- **Crawling**: Multi-threaded crawling with robots.txt compliance
- **Indexing**: Efficient text processing with stemming
- **Search**: Fast query processing with MongoDB aggregation
- **UI**: Responsive design with real-time updates

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- JSoup for HTML parsing
- Porter Stemmer for text processing
- Spring Boot for the backend framework
- Vue.js for the frontend framework
- Tailwind CSS for styling
