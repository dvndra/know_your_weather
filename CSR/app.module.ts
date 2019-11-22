import { IpapiService } from './ipapi.service';
import { DailyService } from './daily.service';
import { CurrentService } from './current.service';
import { SearchService } from './search.service';
import { GeocodingService } from './geocoding.service';
import { AutocompleteService } from './autocomplete.service';

import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule }   from '@angular/forms';
import { HttpModule } from '@angular/http';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatTabsModule} from '@angular/material/tabs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import {MatButtonModule} from '@angular/material/button';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ChartsModule } from 'ng2-charts';
import {MatTableModule} from '@angular/material/table';
import { MatTableDataSource } from '@angular/material/table';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SearchFormComponent } from './search-form/search-form.component';

import { TabResultsComponent } from './tab-results/tab-results.component';

@NgModule({
  declarations: [
    AppComponent,
    SearchFormComponent,
    TabResultsComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule, 
    FormsModule,
    HttpModule,
    MatAutocompleteModule,
    MatTabsModule,
    BrowserAnimationsModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
    MatDividerModule,
    NgbModule,
    ChartsModule,
    MatTableModule,
  ],
  providers: [AutocompleteService,
    GeocodingService,
    SearchService,
    CurrentService,
    DailyService,
    IpapiService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
