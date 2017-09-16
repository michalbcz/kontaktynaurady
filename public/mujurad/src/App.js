import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import SearchForm from './SearchForm';
import Results from './Results';

class App extends Component {

   constructor(props) {
    super(props);
    this.state = {name: props.name};
  }

  componentDidMount() {
    this.setState({ name: 'Michal'})
  }

  render() {
    return (
      <div className="container">
        <SearchForm />
        <Results />
      </div>
    );
  }
}

export default App;
