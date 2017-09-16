import React from 'react';

export default class SearchForm extends React.Component {
    render() {
        return (
            <div className="row justify-content-center search-component">
            <div className="col-sm-12 col-md-8 col-lg-6">
              <form className="my-2 my-lg-0">
                <div className="form-group row">
                  <div className="col-sm-12 col-md-8">
                    <input className="form-control mr-sm-2" type="text" placeholder="Jaký úřad hledáte?" />
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

    search = () => {
    }
}