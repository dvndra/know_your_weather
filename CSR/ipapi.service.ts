import { Injectable } from '@angular/core';
import { Http } from '@angular/http';

@Injectable({
  providedIn: 'root'
})
export class IpapiService {

  private url = "http://ip-api.com/json";
  constructor(private http : Http) { }

  getData(){
    return this.http.get(this.url);
  }
}


