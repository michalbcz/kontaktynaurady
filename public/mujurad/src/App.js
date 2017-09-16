import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import SearchForm from './SearchForm';
import Results from './Results';

class App extends Component {

   constructor(props) {
    super(props);
    this.state = { searchResults: null }
  }

  componentDidMount() {
    
  }

  onSearchResultsChanged = (data) => {
    this.setState({ searchResults: data })
  }

  render() {
    return (
      <div className="container">
        <SearchForm onSearchResultsChanged={ this.onSearchResultsChanged } />
        <Results searchResults = { this.state.searchResults } />
      </div>
    );
  }
}

export default App;
