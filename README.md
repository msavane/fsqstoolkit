# 🧪 FSQS Toolkit – Flexible Scenario-Based QA CLI

**FSQS Toolkit** is a powerful, scriptable **command-line tool** designed for **QA testers**, **BAS professionals**, and **non-technical stakeholders** who need a fast and flexible way to create, run, and manage different types of test scenarios.

Whether you're validating **UI workflows**, writing **Gherkin features**, or testing **REST APIs**, FSQS Toolkit simplifies the process with interactive prompts and file-based execution styles.

---

## 🚀 Features

- 🎉 Interactive console runner for building or loading test cases
- 📄 Supports:
  - FSQS legacy test scripts (`.txt`)
  - Gherkin/Cucumber-style `.feature` files
  - REST API test flows
- 🛠 Save and load test cases from disk
- 👨‍💻 Suitable for manual testers, automation engineers, and stakeholders
- 💻 Pure CLI — lightweight and system-friendly

---

## 🏁 Getting Started

### 📦 Clone the project

```bash
git clone https://github.com/msavane/fsqstoolkit.git
cd fsqstoolkit
🔧 Build & Run
If you're using Java and Maven:


mvn clean install
java -cp target/fsqstoolkit-*.jar runner.ConsoleRunner
Or if compiled already:


java runner.ConsoleRunner
🧭 How It Works
You'll be guided through:

Choosing a test style (legacy, Gherkin, API)

Loading an existing script or creating a new one

Executing the test case

Saving the test case for future use

Example console flow:


🎉 Welcome to FSQS Toolkit!
How would you like to create a test?
1. Load from file
2. Create new test case
Select option [1/2]:
📁 File Structure

src/
├── main/
│   └── java/
│       ├── runner/               # Console entry point
│       ├── builder/              # TestCaseBuilder
│       ├── parser/               # File/script parser
│       ├── service/              # TestCaseService
│       └── util/                 # File discovery, helpers
└── resources/
    └── testcases/               # Your .txt or .feature test files
📃 License
This project is licensed under the MIT License. Free to use, modify, and distribute.

🤝 Contributing
We welcome contributions! See CONTRIBUTING.md for guidelines.

🛡 Security Policy
Please report any vulnerabilities to [https://www.linkedin.com/in/mory-savané-63337220/].

🔗 Related
Gherkin language reference: https://cucumber.io/docs/gherkin/

OpenAPI REST tools: https://swagger.io/tools/

Made with ❤️ to simplify software testing for everyone.
