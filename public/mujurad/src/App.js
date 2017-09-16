import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';

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
        <div className="row justify-content-center search-component">
          <div className="col-sm-12 col-md-8 col-lg-6">
            <form className="my-2 my-lg-0">
              <div className="form-group row">
                <div className="col-sm-12 col-md-8">
                  <input className="form-control mr-sm-2" type="text" placeholder="Jaký úřad hledáte?" />
                </div>
                <div className="col-sm-12 col-md-4">
                  <button className="btn btn-outline-success my-2 my-sm-0" type="submit" aria-label="Hledat">
                    <i className="fa fa-search fa-lg" aria-hidden="true"></i>
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>

        <table className="table">
          <tbody>
            <tr>
              <td>Mestsky urad Picin</td>
              <td>[google places]</td>
              <td>[word cloud]</td>
              <td>[hejtmail]</td>
            </tr>
          </tbody>
        </table>
      </div>
    );
  }
}

export default App;
