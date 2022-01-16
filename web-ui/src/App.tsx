import "./App.css";
import { Link, BrowserRouter, Routes, Route } from "react-router-dom";
import { Search } from "./Pages/Search";
import ReactGA from "react-ga4";

const Home = () => {
  return (
    <div className="App">
      <header className="App-header">
      <Link to="/search">Search</Link>
      </header>
    </div>
  );
};

function App() {
  ReactGA.initialize('G-NLVF1S815F');
  
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Search />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;