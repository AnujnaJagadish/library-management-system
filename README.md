### Library Management App

This is a project to demonstrate java skills, as part of NEU INFO5100.

SpringBoot for backend and Swing for UI. This was the requirement for the project.


## Instructions to run the application

# 1.Install Postgresql
    Use Homebrew to install PostgreSQL:
    brew update
    brew install postgresql
    brew services start postgresql
    psql postgres

  i.Create a SuperUser 

    CREATE ROLE my_superuser WITH SUPERUSER CREATEDB CREATEROLE LOGIN PASSWORD 'your_password';

  ii.Create a database with name ex: library_management

    CREATE DATABASE library_management;

  iii.Set username and password for the database connection
  
    CREATE USER library_user WITH PASSWORD 'your_password';
    GRANT ALL PRIVILEGES ON DATABASE library_management TO library_user;
    \c library_management
   
  iv. Create tables as below:

    CREATE TABLE BOOKS (
    ID SERIAL PRIMARY KEY,
    TITLE VARCHAR(100) NOT NULL,
    AUTHOR VARCHAR(100) NOT NULL,
    GENRE VARCHAR(20) NOT NULL,
    COUNT INTEGER NOT NULL CHECK (COUNT >= 0),
    RESERVED_COUNT INTEGER DEFAULT 0 CHECK (RESERVED_COUNT >= 0),
    BORROWED_COUNT INTEGER DEFAULT 0 CHECK (BORROWED_COUNT >= 0)
        );

    CREATE TABLE MEMBERS (
    ID SERIAL PRIMARY KEY,
    NAME VARCHAR(50) NOT NULL,
    EMAIL VARCHAR(50) UNIQUE NOT NULL,
    PASSWORD VARCHAR(50) NOT NULL,
    ROLE VARCHAR(10) NOT NULL DEFAULT 'member' CHECK (ROLE IN ('admin', 'member'))
    );

    CREATE TABLE RESERVATIONS (
    ID SERIAL PRIMARY KEY,
    BOOK_ID INTEGER REFERENCES BOOKS(ID) ON DELETE CASCADE,
    MEMBER_ID INTEGER REFERENCES MEMBERS(ID) ON DELETE CASCADE,
    ACTION_TYPE VARCHAR(10) NOT NULL CHECK (ACTION_TYPE IN ('RESERVE', 'BORROW', 'RETURN')),
    RESERVED_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    BORROWED_DATE TIMESTAMP,
    RETURNED_DATE TIMESTAMP,
    DUE_DATE TIMESTAMP
    );

    CREATE TABLE REVIEWS (
    ID SERIAL PRIMARY KEY,
    BOOK_ID INTEGER NOT NULL REFERENCES BOOKS(ID) ON DELETE CASCADE,
    MEMBER_ID INTEGER NOT NULL REFERENCES MEMBERS(ID) ON DELETE CASCADE,
    RATING INTEGER NOT NULL CHECK (RATING >= 1 AND RATING <= 5),
    REVIEW_TEXT TEXT,
    REVIEW_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE notifications (
        id SERIAL PRIMARY KEY,
        member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
        message TEXT NOT NULL,
        is_read BOOLEAN DEFAULT FALSE NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    );

    ALTER TABLE notifications
    ADD COLUMN reservation_id BIGINT;

    ALTER TABLE notifications
    ADD CONSTRAINT fk_reservation
    FOREIGN KEY (reservation_id)
    REFERENCES reservations (id)
    ON DELETE CASCADE;


	INSERT INTO MEMBERS (NAME, EMAIL, PASSWORD, ROLE)
    VALUES 
    ('John Admin', 'admin@example.com', 'hashed_password_admin', 'admin'); -- Admin user

# 2. Create a folder named "library-management-system" and perform a git pull from the origin to your local repository.
    mkdir library-management-system
    cd library-management-system
    git clone <repository-url> 

    Once completed, navigate to the following path:
    src/main/java/com/librarysystem/config/DatabaseConfig.java
    Update the following lines:
        dataSource.setUrl("jdbc:postgresql://");
        dataSource.setUsername("");
        dataSource.setPassword("");

# 3.Add PostgreSQL Dependency
    Make sure the build.gradle file includes the PostgreSQL JDBC driver dependency:

    dependencies {
        implementation("org.postgresql:postgresql:42.6.0") // Use the latest version
        // Other dependencies...
    }

   Run the following command to refresh the dependencies:
    ./gradlew build

# 4.Application Properties Configuration

    Ensure the application.properties  file has the correct PostgreSQL connection settings:
    PATH: src/main/resources/application.properties
    # PostgreSQL Database Configuration
    spring.datasource.url=jdbc:postgresql://''
    spring.datasource.username=''
    spring.datasource.password=''

# 5. Clean and Build the Project
   ./gradlew clean build


# 6. Run the Application
   ./gradlew bootRun

# 7.Run LoginFrame.java
    right-click on LoginFrame.java and select Run.
    PATH: src/main/java/com/librarysystem/ui/LoginFrame.java
