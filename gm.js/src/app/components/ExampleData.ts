
export function ExampleData1() {
  return {
    "nextUniqueId": 2000,
    "projects": [
      {
        "name": "aaa",
        "incomeCategory": "income_cat1",
        "incomes": [
          {
            "date": "2015-01-01",
            "foreignAmount":"1000",
            "exchangeRate": "305",
            "localAmount": "305000",
            "id": 11,
          }
        ],
        "expenses": [
          {
            "date": "2015-02-02",
            "localAmount": "1480",
            "foreignAmount": "4.852459016393442622950819672131",
            "category": "cat1",
            "id": 21,
          },
          {
            "date": "2015-02-05",
            "localAmount": "990",
            "category": "cat2",
            "id": 22,
          }
        ],
        "categories": [
          {
            "tagName": "cat1",
            "id": 31
          },
          {
            "tagName": "cat2",
            "id": 32
          }
        ],
        "foreignCurrency":"USD"
      },
      {
        "name": "bbb",
        "incomes": [],
        "expenses": [],
        "categories": [],
        "foreignCurrency": "EUR"
      }
    ],
    "budgetCategories": {
      "name": "Budget",
      "subTags": [
        {
          "name": "Expenses",
          "subTags": [
            {
              "name": "cat1",
              "subTags": [],
            },
            {
              "name": "cat2",
              "subTags": [],
            }
          ],
        },
        {
          "name": "Incomes",
          "subTags": [
            {
              "name": "income_cat1",
              "subTags": [],
            },
            {
              "name": "income_cat2",
              "subTags": [],
            }
          ],
        },
      ],
    },
    "currencies": [
      {"name":"HUF", id: 41},
      {"name":"USD", id: 42},
      {"name":"EUR", id: 43}
    ],
    "localCurrency":"HUF"
  }
};

export function ExampleData2() {
  var data = ExampleData1();
  for (var i = 1; i < 1000; ++i) {
    data.projects[0].expenses.push(
      {
        date: '2015-01-' + (i).toString(),
        localAmount: (i + 1).toString(),
        category: 'cat' + (i % 2 + 1).toString(),
        id: 1000 + i,
      }
    );
  }
  return data;
}
