import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpHeaders } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';

import { ITEMS_PER_PAGE } from 'app/shared/constants/pagination.constants';
import { Audit } from './audit.model';
import { AuditsService } from './audits.service';

@Component({
  selector: 'jhi-audit',
  templateUrl: './audits.component.html',
})
export class AuditsComponent implements OnInit {
  audits?: Audit[];
  firstCall = true;
  fromDate = '';
  itemsPerPage = ITEMS_PER_PAGE;
  page!: number;
  predicate!: string;
  previousPage!: number;
  ascending!: boolean;
  toDate = '';
  totalItems = 0;

  private dateFormat = 'yyyy-MM-dd';

  constructor(
    private auditsService: AuditsService,
    private activatedRoute: ActivatedRoute,
    private datePipe: DatePipe,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.toDate = this.today();
    this.fromDate = this.previousMonth();
    this.activatedRoute.data.subscribe(data => {
      this.page = data['pagingParams'].page;
      this.previousPage = data['pagingParams'].page;
      this.ascending = data['pagingParams'].ascending;
      this.predicate = data['pagingParams'].predicate;
      this.loadData();
    });
    this.handleBackNavigation();
  }

  loadPage(page: number): void {
    this.firstCall = false;
    this.previousPage = page;
    this.transition();
  }

  handleBackNavigation(): void {
    this.activatedRoute.queryParamMap.subscribe((params: ParamMap) => {
      if (!this.firstCall) {
        let prevPage = params.get('page');
        if (prevPage === null) {
          prevPage = '1'; // because there are no params in the URL the first time /admin/audits
        }
        const prevSort = params.get('sort');
        const prevSortSplit = prevSort?.split(',');
        if (prevSortSplit) {
          this.predicate = prevSortSplit[0];
          this.ascending = prevSortSplit[1] === 'asc';
        } else {
          this.predicate = 'auditEventDate';
          this.ascending = false;
        }
        if (+prevPage !== this.page) {
          this.page = +prevPage;
        }
        this.loadPage(this.page);
      }
    });
  }

  canLoad(): boolean {
    return this.fromDate !== '' && this.toDate !== '';
  }

  transition(): void {
    this.firstCall = false;
    if (this.canLoad()) {
      this.router.navigate(['/admin/audits'], {
        queryParams: {
          page: this.page,
          sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc'),
        },
      });
      this.loadData();
    }
  }

  private previousMonth(): string {
    let date = new Date();
    if (date.getMonth() === 0) {
      date = new Date(date.getFullYear() - 1, 11, date.getDate());
    } else {
      date = new Date(date.getFullYear(), date.getMonth() - 1, date.getDate());
    }
    return this.datePipe.transform(date, this.dateFormat)!;
  }

  private today(): string {
    // Today + 1 day - needed if the current day must be included
    const date = new Date();
    date.setDate(date.getDate() + 1);
    return this.datePipe.transform(date, this.dateFormat)!;
  }

  private loadData(): void {
    this.auditsService
      .query({
        page: this.page - 1,
        size: this.itemsPerPage,
        sort: this.sort(),
        fromDate: this.fromDate,
        toDate: this.toDate,
      })
      .subscribe((res: HttpResponse<Audit[]>) => this.onSuccess(res.body, res.headers));
  }

  private sort(): string[] {
    const result = [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  private onSuccess(audits: Audit[] | null, headers: HttpHeaders): void {
    this.totalItems = Number(headers.get('X-Total-Count'));
    this.audits = audits || [];
  }
}
