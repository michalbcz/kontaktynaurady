import React from 'react';

export default class SearchForm extends React.Component {
    constructor(props) {
        super(props);
        this.state = { searchText: '' };
    }

    render() {
        return (
            <div className="row justify-content-center search-component">
            <div className="col-sm-12 col-md-8 col-lg-6">
              <form className="my-2 my-lg-0">
                <div className="form-group row">
                  <div className="col-sm-12 col-md-8">
                    <input 
                        type="text" 
                        value={this.state.searchText} 
                        onChange={this.searchTextChange}
                        className="form-control mr-sm-2" 
                        placeholder="Jaký úřad hledáte?" />
                  </div>
                  <div className="col-sm-12 col-md-4">
                    <button onClick={this.search} className="btn btn-outline-success my-2 my-sm-0" type="submit" aria-label="Hledat">
                      <i className="fa fa-search fa-lg" aria-hidden="true"></i>
                    </button>
                  </div>
                </div>
              </form>
            </div>
          </div>
        );
    }

    searchTextChange = (e) => {
        this.setState({searchText: e.target.value});
    }    

    search = (e) => {
        // alert("Search for " + this.state.searchText);
    }
}